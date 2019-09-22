<%-- 
    Document   : newjsp
    Created on : Jan 31, 2019, 6:50:22 PM
    Author     : alamz
--%>

<%@include file="WEB-INF/jspf/header.jspf" %>


        <script>
              // var ca=["the","be","to","of","and","a","in","that","have","i","it","for","not","on","with","he","as","you","do","at","this","but","his","by","from","they","we","say","her","she","or","an","will","my","one","all","would","there","their","what","so","up","out","if","about","who","get","which","go","me","when","make","can","like","time","no","just","him","know","take","people","into","year","your","good","some","could","them","see","other","than","then","now","look","only","come","its","over","think","also","back","after","use","two","how","our","work","first","well","way","even","new","want","because","any","these","give","day","most","us","are"];
       </script>
        
           
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

	 videos.push({videoId: 't5hTIBhcnq8', startSeconds: 5, endSeconds: 8})
	 videos.push({videoId: 'rKtFJxGzJX8', startSeconds: 22, endSeconds: 26})
	 videos.push({videoId: 'j9NtkNrBmJg', startSeconds: 15, endSeconds: 19})               
      /*
	  videos.push({videoId: 'kLkSukx1sCw', startSeconds: 20, endSeconds: 25})   
	  videos.push({videoId: 'gWL-r72tGOE', startSeconds: 30, endSeconds: 35})   
   */
        
  function onYouTubeIframeAPIReady() {
		player = new YT.Player("player", {
			height: "390",
			width: "640",
			 playerVars: { 'controls':1,  'suggestedQuality': 'large',  'rel':0, 'enablejsapi':1},
	   	events: {
				"onReady": onPlayerReady,
				"onStateChange": onPlayerStateChange
			}
		});
	}

	function onPlayerReady(event) {
     	state = "ready";
    
    $("#btnPause").hide();
    $("#btnReplay").hide();
           
    
      player.cueVideoById({
               'videoId': videos[index].videoId,
               'startSeconds': videos[index].startSeconds,
               'endSeconds': videos[index].endSeconds,
     });    
	 	
	 populateTimeline();
	 for(var i=0; i<videos.length; i++){
	 testLabel.innerHTML += timeline[i].index + "---" + timeline[i].startTime + "---" + timeline[i].endTime + "</br>";
	 }
   
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
            testLabel.innerHTML = getTotalDuration() + " " + seekToWhole ;
          //if(seekToWhole == getTotalDuration()){
           // player.stopVideo();
          //}else{
          
          // if(index < videos.length){
          if(seekToWhole < getTotalDuration()){
          for(i = 0; i<timeline.length; i++ ){
            if(seekToWhole >= timeline[i].startTime && seekToWhole <= timeline[i].endTime){
            index = i;
         	  seekToSingle =  seekToWhole - timeline[i].startTime;
                player.cueVideoById({
               'videoId': videos[i].videoId,
               //'startSeconds': videos[i].startSeconds,
			         'startSeconds': videos[i].startSeconds + seekToSingle,
               'endSeconds': videos[i].endSeconds,
             });
              if(playing){                
                player.playVideo();                
              } 
			//testLabel.innerHTML = "seekToWhole " + seekToWhole +  "</br>  timeline[i].startTime " + timeline[i].startTime   +  "</br>  seekToSingle " + seekToSingle  + "</br>  i " + i + "</br>" + "Current time " + playing;
			 //player.seekTo(seekToSingle, true);
            }
          }
          }
               
       // player.seekTo(seekToSingle, true);
       },
				stop: function() {
          //alert("stopped...")
					//seekVideo = false;
          //player.playVideo();         
				}
			}
		);
    
		setInterval(function() {
			if(!seekVideo && index < videos.length) {
	      //if ( player.getVideoLoadedFraction() > 0) {
        testLabel.innerHTML = player.getPlayerState() + " " + playing;
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
      //alert("unstarted");
			break;	//unstarted
			case 0:
        //alert("ended");
        //state = "ended";
        //index++;
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
        //alert("paused");
			state = "paused";
      //playing = false;
           $("#btnReplay").hide();
           $("#btnPause").hide();
           $("#btnPlay").show();

			break; //paused
			case 3:
        //alert("buffering");
      break; //buffering
			case 5:
        //alert("cued");
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
      } /*else {
        index = 0;
        player.loadVideoById({
          'videoId': videos[index].videoId,
          'startSeconds': videos[index].startSeconds,
          'endSeconds': videos[index].endSeconds,
        });		
      }      */
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
           //for(i = 0; i<timeline.length; i++ ){
           //ellapsedTime = ellapsedTime + timeline[i].duration;     
            for(var i = 0; i<index; i++ ){
            //ellapsedTime = ellapsedTime + getDuration(i);
            ellapsedTime = ellapsedTime + timeline[i].duration;     
          }
        }
          //if(index < videos.lenght){
        	currentTime = ellapsedTime + player.getCurrentTime() - getStartSeconds(index);
          //}
 
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
         //alert(seekToSingle + "----" +seekToWhole+ " ------ "+ellapsedDuration + "------ " + vId + " ------" + ss + "----- " +es + "----" + i );

         player.loadVideoById({
                     'videoId': vId,
                     'startSeconds': seekToSingle,
                     'endSeconds': es,
                  });
        //player.seekTo(seekToSingle, true);
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
 &nbsp; &nbsp;&nbsp;
  <div margin="25px" padding="25px">
  </br>
 
        <label id="testLabel" />
  </div>
  
       </div>   
       </div>
        
<%@include file="WEB-INF/jspf/footer.jspf" %>