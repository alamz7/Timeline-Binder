<%-- 
    Document   : player
    Created on : Dec 30, 2018, 2:07:14 AM
    Author     : alamz
--%>
<%@page import="java.util.List"%>
<%@page import="timelinebinder.Concept"%>
<%@page import="java.util.ArrayList"%>
<%@include file="WEB-INF/jspf/header.jspf" %>

<%  
// retrieve your list from the request, with casting 
    Concept _concept =  (Concept)request.getAttribute("concept");

    List<String> videoIds = new ArrayList<String>();
    List<String> startTimes = new ArrayList<String>();
    List<String> endTimes = new ArrayList<String>();
    if( _concept != null)
    {    
        for(String str:_concept.getTimedVideos().keySet())
        {
            List<String> newList = new ArrayList<>();
            newList = _concept.getTimedVideos().get(str);
            for(String s:newList)
            {
                String[] tmpArray = s.split("-->");
                videoIds.add(str);
                startTimes.add(tmpArray[0].trim());
                endTimes.add(tmpArray[1].trim());
            }
        }
        System.out.println("-------------------------------");     
        System.out.println(videoIds.size());         
        System.out.println("-------------------------------");     
    }
%>

<div id="body">
<div id="site_content">
    <script  src="https://code.jquery.com/jquery-3.3.1.min.js"></script>
    <script  src="https://code.jquery.com/ui/1.12.1/jquery-ui.min.js"></script>
    <script>
    var player;
    var videoDuration = 0;
	  var seekVideo = false;
    var state = "";
    var videos = [];
    var timeline = [];
    var index = 0;
    var origin = "";
    var playing;

	$("#player").html("<h2>loading...</h2>");

	var tag = document.createElement("script");

	tag.src = "https://www.youtube.com/iframe_api";

	var firstScriptTag = document.getElementsByTagName("script")[0];
	firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);


        <%  for(int i=0; i < videoIds.size(); i++){ %>
        videos.push({videoId: '<%= videoIds.get(i) %>', startSeconds: <%= startTimes.get(i) %>, endSeconds: <%= endTimes.get(i) %>})
        <% } %>
         
  function onYouTubeIframeAPIReady() {
		player = new YT.Player("player", {
			height: "390",
			width: "640",
			 playerVars: { 'controls':0,  'suggestedQuality': 'large',  'rel':0, 'enablejsapi':1,'cc_load_policy':1,'iv_load_policy':1, 'cc_lang_pref':'en'},
	   	events: {
				"onReady": onPlayerReady,
				"onStateChange": onPlayerStateChange
			}
		});
	}

	function onPlayerReady(event) {
     	state = "ready";
    populateTimeline();
    $("#btnPause").hide();
    $("#btnReplay").hide();
           
     
      //player.cueVideoById({  //videos would not be autoplayed on start 
      player.loadVideoById({   //videos would be autoplayed on start 
               'videoId': videos[index].videoId,
               'startSeconds': videos[index].startSeconds,
               'endSeconds': videos[index].endSeconds,
     });    
	 	
	 //populateTimeline();
	   
     $("#seekBtn").draggable(
			{
        axis: "x",
				containment: "#seekBarWrapper",
				drag: function(event, ui)
				{
          seekVideo = true;
          $("#seekBar div").css("width", ui.position.left);
					var barwidth = $("#seekBar").width();
					var btnpos = ui.position.left;
					var oran = (btnpos/barwidth)*100;
          var seekToWhole = (getTotalDuration() * oran/100);
          var seekToSingle = 0;
          var ellapsedDuration = 0; 
          var i;
          seekVideo = false;
        

          updateTimer(seekToWhole, getTotalDuration());
          if(seekToWhole < getTotalDuration()){
          for(i = 0; i<timeline.length; i++ ){
            if(seekToWhole >= timeline[i].startTime && seekToWhole <= timeline[i].endTime){
            index = i;
         	  seekToSingle =  seekToWhole - timeline[i].startTime;
                player.cueVideoById({
               'videoId': videos[i].videoId,
               'startSeconds': videos[i].startSeconds + seekToSingle,
               'endSeconds': videos[i].endSeconds,
             });
              if(playing){                
                player.playVideo();                
              } 		
            }
          }
          }       
       },
	    stop: function() {}
			}
	);
    
	setInterval(function() {
		if(!seekVideo && index < videos.length) {
	if(player.getPlayerState() == 1){
        curTime = getCurrentTime();
				var barwidth = $("#seekBar").width();
				var oran = (curTime/getTotalDuration())*100;  
				var oran2 = barwidth*oran/100;
        $("#seekBtn").css("left", oran2);
				$("#seekBar div").css("width", $("#seekBtn").css("left"));
           
       	if(!isNaN(curTime) && curTime>=0){
				updateTimer(curTime, getTotalDuration());}
         }			
			}
		}, 100);    
	}

	function onPlayerStateChange(event) {

     if (event.data === YT.PlayerState.ENDED && player.getVideoLoadedFraction() > 0) {
        index++;
        if(index < videos.length){
           event.target.loadVideoById({
                     'videoId': videos[index].videoId,
                     'startSeconds': videos[index].startSeconds,
                     'endSeconds': videos[index].endSeconds,
                  });
         } else {
           $("#btnReplay").show();
           $("#btnPause").hide();
           $("#btnPlay").hide();
         }
	    }
		switch(event.data)
		{
			case -1:
     			break;	//unstarted
			case 0:
        break; //ended
			case 1:
        state = "playing";        
        if($("#btnReplay").is(":visible")){
           playing = true; //while dragging the cursor, one shoule know if it was in playing mode or otherwise.
           index = 0;
           player.loadVideoById({
          'videoId': videos[index].videoId,
          'startSeconds': videos[index].startSeconds,
          'endSeconds': videos[index].endSeconds,
        });		     
        }    
           $("#btnReplay").hide();
           $("#btnPlay").hide();
           $("#btnPause").show();
        
      break; //playing
			case 2:
       			state = "paused";
      //playing = false;
           $("#btnReplay").hide();
           $("#btnPause").hide();
           $("#btnPlay").show();

			break; //paused
			case 3:
        break; //buffering
			case 5:
        break; //video cued
		}
	}

	$(function() {
		$(document).on("click", "#btnPlay", function() {
      
      
     $("#btnPause").show();
     $("#btnPlay").hide();
     $("#btnReplay").hide();
      
     playing = true; //while dragging the cursor, one shoule know if it was in playing mode or otherwise.
      
      if(state=="paused" || state == "ready"){
        player.playVideo();
      } 
	});
    
    	$(document).on("click", "#btnReplay", function() {
            
     $("#btnPause").show();
     $("#btnPlay").hide();
     $("#btnReplay").hide();
      
     playing = true; //while dragging the cursor, one shoule know if it was in playing mode or otherwise.
      
        index = 0;
        player.loadVideoById({
          'videoId': videos[index].videoId,
          'startSeconds': videos[index].startSeconds,
          'endSeconds': videos[index].endSeconds,
        });		     
	});

		$(document).on("click", "#btnPause", function() {
     
      $("#btnPlay").show();
      $("#btnPause").hide();
      $("#btnReplay").hide();
      
       playing = false;
			player.pauseVideo();
		});	
	});

    function updateTimer(currentTime, duration){
        var eTime;
        var tTime;

        //calculate elapsed time
        var eSeconds = Math.round(currentTime);
        var eMinutes = Math.floor(eSeconds/60);
        eMinutes = (eMinutes >= 10) ? eMinutes : "0" + eMinutes;
        eSeconds = Math.floor(eSeconds % 60);
        eSeconds = (eSeconds >= 10) ? eSeconds : "0" + eSeconds;

        //calculate total time
        var tSeconds = Math.round(duration);
        var tMinutes = Math.floor(tSeconds/60);
        tMinutes = (tMinutes >= 10) ? tMinutes : "0" + tMinutes;
        tSeconds = Math.floor(tSeconds % 60);
        tSeconds = (tSeconds >= 10) ? tSeconds : "0" + tSeconds;

        //assign these values to our variables
        eTime = '' + eMinutes + ':' + eSeconds;
        tTime = '' + tMinutes + ':' + tSeconds;

        //display in timer
        $('#timer').html('' + eTime + ' / ' + tTime);
    }

    function getStartSeconds(index){
        return videos[index].startSeconds;
	}
	function getEndSeconds(index){
	    return videos[index].endSeconds;
	}
    function getDuration(index){
        return videos[index].endSeconds - videos[index].startSeconds;
    }
    function getTotalDuration(){
      var duration = 0;
      for(i=0; i<videos.length; i++){
         duration = duration + videos[i].endSeconds - videos[i].startSeconds;
      }   
      return duration;
    }
      function getCurrentTime(){
        var currentTime = 0;
        var ellapsedTime = 0;
        if(index > 0){
            for(var i = 0; i<index; i++ ){
            ellapsedTime = ellapsedTime + timeline[i].duration;     
          }
        }
         currentTime = ellapsedTime + player.getCurrentTime() - getStartSeconds(index);
         
          if(currentTime < 0){
            currentTime = 0;
          }
         return currentTime;
    }
      function getLocation(seekToWhole){
        var vId;
        var ss;
        var es;
        var seekToSingle;
        var ellapsedDuration = 0; 
        var i;
        for(i = 0; i<index; i++ ){
            ellapsedDuration = ellapsedDuration + getDuration(i);
            if(seekToWhole <= ellapsedDuration){
              vId = videos[i].videoId;
              ss = videos[i].startSeconds;
              es = videos[i].endSeconds;
             i=index + 1; 
            }
          }
              
         seekToSingle =  seekToWhole  - ellapsedDuration; 
         
         player.loadVideoById({
                     'videoId': vId,
                     'startSeconds': seekToSingle,
                     'endSeconds': es,
                  });
    }
	function populateTimeline(){
	 var sTime = 0;
	 var eTime = 0;
	 for(var i = 0; i < videos.length; i++){
	 sTime = eTime;
	 eTime = eTime + getDuration(i);
	 timeline.push({index: i, startTime: sTime, endTime: eTime, duration: getDuration(i) })	
	 }
	}

    </script> 
</head>

<div class="media-container" >
    <div id="player"></div>

    <div class="media-controlbar pr">
        <div>
            <div id="seekBarWrapper">
                <span id="seekBtn"></span>
                <div id="seekBar">
                    <div></div>
                </div>
            </div>
          
            <img id="btnPlay" src="resources/images/play.png" alt="Play" height="18" width="18">
            <img id="btnPause" src="resources/images/pause.png" alt="Pause" height="18" width="18">
            <img id="btnReplay" src="resources/images/replay.png" alt="Replay" height="18" width="18">
            <span id= "timer"  x="64" y="35">00:00 / 00:00</span>
        </div>
    </div>
</div>
</div>   
</div>
  
<%@include file="WEB-INF/jspf/footer.jspf" %>