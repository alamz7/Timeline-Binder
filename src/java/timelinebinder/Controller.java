
package timelinebinder;
import java.awt.Desktop;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import java.lang.String;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {

    private List<Video> videos;                             // List of videos
    private List<Concept> concepts = new ArrayList<Concept>();
    //private static Concept _concept;
    //private Main main; 
    private Notification notification;
    private List<List<NetSubtitle>> lSubsWithTranslations;  // Tracks (item 0) + Targets (item 1)
    
    protected void addTracks(List<NetSubtitle> subtitles) {
        lSubsWithTranslations.get(0).addAll(subtitles);
    }
    
    protected void addTargets(List<NetSubtitle> subtitles) {
        lSubsWithTranslations.get(1).addAll(subtitles);
    }
    
    protected List<NetSubtitle> getTracks() {
        return lSubsWithTranslations.get(0);
    }
    
    protected List<NetSubtitle> getTargets() {
        return lSubsWithTranslations.get(1);
    }
    
    public Controller() {
        notification = new Notification();
        initSubtitlesDataStructure();        
    }  
       
    // Data structure initialisation
    protected final void initSubtitlesDataStructure() {
        lSubsWithTranslations = new ArrayList<List<NetSubtitle>>();
        lSubsWithTranslations.add(new ArrayList<NetSubtitle>());
        lSubsWithTranslations.add(new ArrayList<NetSubtitle>());
    }
    
    // Parses a text file and returns subtitles for each video URL found
    protected void processURLListFile(InputStreamReader isr) {
        videos = Converter.parseURLListFile(isr);
        retrieveSubtitles();
    }
    
    protected void processInputURL(String URL) {
        videos = new ArrayList<Video>();
        videos.add(new Video(URL));
        retrieveSubtitles();
    }
        
    // Returns true if there is not at least 1 track and 1 target
    protected boolean islSubsWithTranslationsNull() {
        List<List<NetSubtitle>> swt = lSubsWithTranslations;
        return (swt == null || swt.size() < 2 ||
                swt.get(0) == null ||  swt.get(0).isEmpty() ||
                swt.get(1) == null ||  swt.get(1).isEmpty());
    }

    // Retrieves LIST of subtitles from the network
    public void retrieveSubtitles() {
        List<List<NetSubtitle>> al;
        List<Video> invalidVideos;
        int videoCount, videoTotalCount;
        
        initSubtitlesDataStructure();
        videoTotalCount = this.videos.size();
        
        invalidVideos = new ArrayList<Video>();
        videoCount = 0;
       // Check if URL is valid
        for (Video v : this.videos) {
            try {
                notification.setStatusMessage("Connecting " + ++videoCount + videoTotalCount);
                al = v.getSubtitlesWithTranslations();
                addTracks(al.get(0)); // lSubsWithTranslations.get(0).addAll(al.get(0));
                if (getTargets().isEmpty()) // Only add targets of the *first video with targets* - technically wrong, it makes sense in practice
                    addTargets(al.get(1)); // lSubsWithTranslations.get(1).addAll(al.get(1));
                    notification.setStatusMessage("");
            } catch (Video.HostNoGV e) {
                if (videoTotalCount == 1) notification.setErrorMessage("msg.url.unknown.host");
                else  notification.setErrorMessage("msg.url.unknown.host " + v.getURL());
                invalidVideos.add(v);
                continue;
            } catch (Video.NoDocId e) {
                if (videoTotalCount == 1)  notification.setErrorMessage("msg.url.parameter.docid.not.found");
                else  notification.setErrorMessage("msg.url.parameter.docid.not.found " +  v.getURL());
                invalidVideos.add(v);
                continue;
            } catch (Video.NoQuery e) {
                if (videoTotalCount == 1)  notification.setErrorMessage("msg.url.parameter.not.found");
                else  notification.setErrorMessage("msg.url.parameter.not.found " + v.getURL());
                invalidVideos.add(v);
                continue;
            } catch (Video.InvalidDocId e) {
                if (videoTotalCount == 1)  notification.setErrorMessage("msg.url.parameter.docid.invalid");
                else  notification.setErrorMessage("msg.url.parameter.not.found " + v.getURL());
                invalidVideos.add(v);
                continue;
            } catch (Video.NoSubs e) {
                if (videoTotalCount == 1)  notification.setErrorMessage("msg.infile.no.subtitles.found");
                else  notification.setErrorMessage("msg.url.parameter.not.found " + v.getURL());
                invalidVideos.add(v);
                continue;
            } catch (MalformedURLException e) {
                if (videoTotalCount == 1)  notification.setErrorMessage("msg.url.invalid.format");
                else  notification.setErrorMessage("msg.url.parameter.not.found " + v.getURL());
                invalidVideos.add(v);
                continue;
            } catch (org.jdom.input.JDOMParseException e) {
                if (videoTotalCount == 1)  notification.setErrorMessage("msg.url.unexpected.format");
                else  notification.setErrorMessage("msg.url.parameter.not.found " + v.getURL());
                invalidVideos.add(v);
                continue;
            } catch (java.net.UnknownHostException e) {
                if (videoTotalCount == 1)  notification.setErrorMessage("msg.net.unknown.host");
                else  notification.setErrorMessage("msg.url.parameter.not.found " + v.getURL());
                invalidVideos.add(v);
                continue;
            } catch (FileNotFoundException e) {
                if (videoTotalCount == 1)  notification.setErrorMessage("msg.url.does.not.exist");
                else  notification.setErrorMessage("msg.url.parameter.not.found " + v.getURL());
                invalidVideos.add(v);
                continue;
            } catch (Video.NoYouTubeParamV e) {
                if (videoTotalCount == 1)  notification.setErrorMessage("msg.net.missing.video.param");
                else notification.setErrorMessage("msg.url.parameter.not.found " + v.getURL());
                invalidVideos.add(v);
                continue;
            } catch (SocketException e) {
                if (videoTotalCount == 1)  notification.setErrorMessage("msg.net.socket.exception");
                else notification.setErrorMessage("msg.url.parameter.not.found " + v.getURL());
                invalidVideos.add(v);
                continue;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                if (videoTotalCount == 1)  notification.setErrorMessage("msg.unknown.error");
                else notification.setErrorMessage("msg.url.parameter.not.found " + v.getURL());
                invalidVideos.add(v);
                continue;
            }
        }
        
        // Removing invalid "videos" (text lines not containing a URL or a video without subtitles)
        for (Video v : invalidVideos)
            this.videos.remove(v);
   }
    
    protected void convertSubtitlesTracks() {
        Converter conv;
        Video v;

        Object dataTracks[][];
        String fileName, s;
        List<NetSubtitle> lTracks;
        int i, selectedCountTotalSubtitles, selectedCountTracks = 0;
        boolean fewSubsSkipped = false;
        boolean flag = false; // 'flag' will show if any 'en' srt file has already been selected
        
        InputStreamReader isr;
        
        lTracks = this.getTracks();

        selectedCountTotalSubtitles = lTracks.size(); 
        // For each selected track: only convert 'en' it to SRT
        for ( NetSubtitle ns :lTracks) {
         if("en".equals(ns.getLang()) && !flag )
         {
             flag = true;
                v = ns.getVideo();
                try {
                    // When handling tracks, it is worth to try signature method FOR EACH track,
                    // even when a previous track retrieval via signature method failed
                    if (v.getMagicURL().isEmpty()) throw new Exception("No *Magic* URL!");
                    v.setMethod(NetSubtitle.Method.YouTubeSignature);
                    isr = v.readURL(ns.getTrackURL());
                } catch (Exception ex1) {
                    if (Notification.DEBUG) {
                        System.out.println("(DEBUG) URL could not be read via Signature method...");
                        System.out.println(
                            String.format("(DEBUG) Method=%s, Type=%s, ID=%s, IDXML=%s, Lang=%s, Name=%s, Exception message='%s'",
                                v.getMethod(),
                                ns.getType(),
                                ns.getId(),
                                ns.getIdXML(),
                                ns.getLang(),
                                ns.getName(),
                                ex1.getMessage()));
                    }

                    if (ns.getType() == NetSubtitle.Tipus.YouTubeASRTrack)
                    {
                        // YouTube ASR cannot be retrieved by using Legacy method.
                        if (Notification.DEBUG) {
                            System.out.println("(DEBUG) YouTube ASR cannot be retrieved via Legacy method. Operation partially aborted.");
                            System.out.println(
                                String.format("(DEBUG) Method=%s, Type=%s, ID=%s, IDXML=%s, Lang=%s, Name=%s",
                                    v.getMethod(),
                                    ns.getType(),
                                    ns.getId(),
                                    ns.getIdXML(),
                                    ns.getLang(),
                                    ns.getName()));
                        }
                        fewSubsSkipped = true;
                        if (selectedCountTotalSubtitles == 1)  notification.setErrorMessage("msg.io.cc.unreadable");
                        else notification.setErrorMessage("msg.io.cc.unreadable [" + v.getId() + "," + ns.getLang() + "," + ns.getIdXML() + "]");
                        continue;
                    } else if (ns.getType() == NetSubtitle.Tipus.YouTubeTrack)
                    {
                        // A YouTube track/target can be retrieved by using legacy method.
                        // However, GUI should not reach this point with a target
                        if (Notification.DEBUG) System.out.println("(DEBUG) Switching to YouTube Legacy mode and retrying...");
                        v.setMethod(NetSubtitle.Method.YouTubeLegacy);

                        try
                        {
                            isr = v.readURL(ns.getTrackURL(NetSubtitle.Method.YouTubeLegacy));
                        } catch (Exception ex2) {
                            if (Notification.DEBUG) {
                                System.out.println("(DEBUG) URL could not be read with Legacy method. Operation partially aborted");
                                System.out.println(
                                    String.format("(DEBUG) Method=%s, Type=%s, ID=%s, IDXML=%s, Lang=%s, Name=%s, Exception message='%s'",
                                        v.getMethod(),
                                        ns.getType(),
                                        ns.getId(),
                                        ns.getIdXML(),
                                        ns.getLang(),
                                        ns.getName(),
                                        ex2.getMessage()));
                            }
                            fewSubsSkipped = true;
                            if (selectedCountTotalSubtitles == 1)  notification.setErrorMessage("msg.io.cc.unreadable");
                            else notification.setErrorMessage("msg.io.cc.unreadable [" + v.getId() + "," + ns.getLang() + "," + ns.getIdXML() + "]");
                            continue;
                        }
                    } else 
                    {
                        // YouTube Target should not reach this point due to GUI.
                        // Google Track should not reach this point.
                        if (Notification.DEBUG) System.out.println("(DEBUG) Entered wrong section of code. Unexpected result.");
                        fewSubsSkipped = true;                        
                        if (selectedCountTotalSubtitles == 1)  notification.setErrorMessage("msg.io.cc.unreadable");
                        else  notification.setErrorMessage("msg.io.cc.unreadable [" + v.getId() + "," + ns.getLang() + "," + ns.getIdXML() + "]");
                        continue;
                    }
                }

                fileName = "";
                fileName += ns.getId();
                fileName += ".srt";
              
                String str = Common.UPLOADDIRECTORY;
                File dir = new File(str);
                if (!dir.exists()) {
                Common.makeDirectory(str);
                }
                conv = new Converter(
                    isr,
                    Common.returnDirectory(str) + fileName, 
                    0,        //gui.getDelay(),
                    false);          
                
                if (!conv.run()) 
                {
                    // Conversion failed
                    // If Signature method was used and type is Track, let's retry
                    // Otherwise, operation is partially aborted
                    if (v.getMethod() == NetSubtitle.Method.YouTubeSignature && 
                            ns.getType() == NetSubtitle.Tipus.YouTubeTrack)
                    {
                        // A YouTube track/target can be retrieved by using legacy method.
                        if (Notification.DEBUG) System.out.println("(DEBUG) Switching to YouTube Legacy mode and retrying...");
                        v.setMethod(NetSubtitle.Method.YouTubeLegacy);

                        try
                        {
                            isr = v.readURL(ns.getTrackURL(NetSubtitle.Method.YouTubeLegacy));
                        } catch (Exception ex1) {
                            if (Notification.DEBUG) {
                                System.out.println("(DEBUG) URL could not be read with Legacy method. Operation partially aborted");
                                System.out.println(
                                    String.format("(DEBUG) Method=%s, Type=%s, ID=%s, IDXML=%s, Lang=%s, Name=%s, Exception message='%s'",
                                        v.getMethod(),
                                        ns.getType(),
                                        ns.getId(),
                                        ns.getIdXML(),
                                        ns.getLang(),
                                        ns.getName(),
                                        ex1.getMessage()));
                            }
                            fewSubsSkipped = true;
                            continue;
                        }

                        conv = new Converter(
                            isr,
                            //Common.returnDirectory("C:\\Users\\alamz\\Desktop") + fileName, 
                            Common.returnDirectory(str) + fileName,
                            0,              //gui.getDelay(),
                            false);          

                        conv.run();
                    }
                }
                selectedCountTracks++;
            //}
        }
        if (! fewSubsSkipped)  notification.setMessage("msg.conversion.finished");
        }
    }
   
     public void ModifyFile()
    {
        List<String> videoIds = new ArrayList<String>();
        videoIds = getVideoIds();
        for(String id:videoIds)
        {
            Path path = Paths.get(Common.returnDirectory(Common.UPLOADDIRECTORY) + id + ".srt");
            Charset charset = StandardCharsets.UTF_8;
            try
            {
                String content = new String(Files.readAllBytes(path), charset);
                content = content.replaceAll("<[^>]*>", "");  
                content = content.replaceAll("\\[.*?\\]","");

                String[] sWords = {"a","about","above","after","again","against","ain","all","am","an","and","any","are","aren","aren't","as","at","be","because","been","before","being","below","between","both","but","by","can","couldn","couldn't","d","did","didn","didn't","do","does","doesn","doesn't","doing","don","don't","down","during","each","few","for","from","further","had","hadn","hadn't","has","hasn","hasn't","have","haven","haven't","having","he","her","here","hers","herself","him","himself","his","how","i","if","in","into","is","isn","isn't","it","it's","its","itself","just","ll","m","ma","me","mightn","mightn't","more","most","mustn","mustn't","my","myself","needn","needn't","no","nor","not","now","o","of","off","on","once","only","or","other","our","ours","ourselves","out","over","own","re","s","same","shan","shan't","she","she's","should","should've","shouldn","shouldn't","so","some","such","t","than","that","that'll","the","their","theirs","them","themselves","then","there","these","they","this","those","through","to","too","under","until","up","ve","very","was","wasn","wasn't","we","were","weren","weren't","what","when","where","which","while","who","whom","why","will","with","won","won't","wouldn","wouldn't","y","you","you'd","you'll","you're","you've","your","yours","yourself","yourselves","could","he'd","he'll","he's","here's","how's","i'd","i'll","i'm","i've","let's","ought","she'd","she'll","that's","there's","they'd","they'll","they're","they've","we'd","we'll","we're","we've","what's","when's","where's","who's","why's","would","able","abst","accordance","according","accordingly","across","act","actually","added","adj","affected","affecting","affects","afterwards","ah","almost","alone","along","already","also","although","always","among","amongst","announce","another","anybody","anyhow","anymore","anyone","anything","anyway","anyways","anywhere","apparently","approximately","arent","arise","around","aside","ask","asking","auth","available","away","awfully","b","back","became","become","becomes","becoming","beforehand","begin","beginning","beginnings","begins","behind","believe","beside","besides","beyond","biol","brief","briefly","c","ca","came","cannot","can't","cause","causes","certain","certainly","co","com","come","comes","contain","containing","contains","couldnt","date","different","done","downwards","due","e","ed","edu","effect","eg","eight","eighty","either","else","elsewhere","end","ending","enough","especially","et","etc","even","ever","every","everybody","everyone","everything","everywhere","ex","except","f","far","ff","fifth","first","five","fix","followed","following","follows","former","formerly","forth","found","four","furthermore","g","gave","get","gets","getting","give","given","gives","giving","go","goes","gone","got","gotten","h","happens","hardly","hed","hence","hereafter","hereby","herein","heres","hereupon","hes","hi","hid","hither","home","howbeit","however","hundred","id","ie","im","immediate","immediately","importance","important","inc","indeed","index","information","instead","invention","inward","itd","it'll","j","k","keep","keeps","kept","kg","km","know","known","knows","l","largely","last","lately","later","latter","latterly","least","less","lest","let","lets","like","liked","likely","line","little","'ll","look","looking","looks","ltd","made","mainly","make","makes","many","may","maybe","mean","means","meantime","meanwhile","merely","mg","might","million","miss","ml","moreover","mostly","mr","mrs","much","mug","must","n","na","name","namely","nay","nd","near","nearly","necessarily","necessary","need","needs","neither","never","nevertheless","new","next","nine","ninety","nobody","non","none","nonetheless","noone","normally","nos","noted","nothing","nowhere","obtain","obtained","obviously","often","oh","ok","okay","old","omitted","one","ones","onto","ord","others","otherwise","outside","overall","owing","p","page","pages","part","particular","particularly","past","per","perhaps","placed","please","plus","poorly","possible","possibly","potentially","pp","predominantly","present","previously","primarily","probably","promptly","proud","provides","put","q","que","quickly","quite","qv","r","ran","rather","rd","readily","really","recent","recently","ref","refs","regarding","regardless","regards","related","relatively","research","respectively","resulted","resulting","results","right","run","said","saw","say","saying","says","sec","section","see","seeing","seem","seemed","seeming","seems","seen","self","selves","sent","seven","several","shall","shed","shes","show","showed","shown","showns","shows","significant","significantly","similar","similarly","since","six","slightly","somebody","somehow","someone","somethan","something","sometime","sometimes","somewhat","somewhere","soon","sorry","specifically","specified","specify","specifying","still","stop","strongly","sub","substantially","successfully","sufficiently","suggest","sup","sure","take","taken","taking","tell","tends","th","thank","thanks","thanx","thats","that've","thence","thereafter","thereby","thered","therefore","therein","there'll","thereof","therere","theres","thereto","thereupon","there've","theyd","theyre","think","thou","though","thoughh","thousand","throug","throughout","thru","thus","til","tip","together","took","toward","towards","tried","tries","truly","try","trying","ts","twice","two","u","un","unfortunately","unless","unlike","unlikely","unto","upon","ups","us","use","used","useful","usefully","usefulness","uses","using","usually","v","value","various","'ve","via","viz","vol","vols","vs","w","want","wants","wasnt","way","wed","welcome","went","werent","whatever","what'll","whats","whence","whenever","whereafter","whereas","whereby","wherein","wheres","whereupon","wherever","whether","whim","whither","whod","whoever","whole","who'll","whomever","whos","whose","widely","willing","wish","within","without","wont","words","world","wouldnt","www","x","yes","yet","youd","youre","z","zero","a's","ain't","allow","allows","apart","appear","appreciate","appropriate","associated","best","better","c'mon","c's","cant","changes","clearly","concerning","consequently","consider","considering","corresponding","course","currently","definitely","described","despite","entirely","exactly","example","going","greetings","hello","help","hopefully","ignored","inasmuch","indicate","indicated","indicates","inner","insofar","it'd","keep","keeps","novel","presumably","reasonably","second","secondly","sensible","serious","seriously","sure","t's","third","thorough","thoroughly","three","well","wonder","time","people","year","good","work","day","remember","thrice","ten","cool","hot","today","tomorrow","yesterday","difference","add","fast","slow","cover","mute","front","top","amoungst","amount","bill","bottom","call","con","cry","de","describe","detail","eleven","empty","fifteen","fify","fill","find","fire","forty","full","hasnt","interest","mill","mine","move","side","sincere","sixty","thickv","thin","twelve","twenty","thing","things","rear","run","running","start","started","change"};
                content  = content.toLowerCase();
                for(String str: sWords)
                {
                    content = content.replaceAll("\\b" + str + "\\b", "");                
                }

                content = content.trim().replaceAll("\'","");
                Files.write(path, content.getBytes(charset));
            }
            catch(Exception ex)
            {
            }
        }
    }
    
    public List<Concept> GetKeywords()
    {
        List<String> videoIds = new ArrayList<String>();
        videoIds = getVideoIds();
        for(String id:videoIds)
        {
            String fileName = Common.returnDirectory(Common.UPLOADDIRECTORY) + id + ".srt";
            String timestamp = "";
            try
            {
                Scanner scan = new Scanner(new File(fileName));
                while(scan.hasNext()){
                    String line = scan.nextLine().toLowerCase();
                    if(!"".equals(line)) 
                    {
                        Pattern p = Pattern.compile("^\\d+$");
                        Matcher m = p.matcher(line);
                        if (m.find())
                        {

                        }
                        else 
                        {          
                            p = Pattern.compile("(\\d+)+\\s+(-->)+\\s+(\\d+)");
                            m = p.matcher(line);
                            if (m.find())
                            {
                                timestamp = line.trim();
                            }
                            else
                            {
                                if(!"".equals(line.trim()) && line.trim().length() > 1){

                                    line = line.replaceAll("\\s+", " ");     //eliminates white spaces from text
                                    line = line.trim();                      // eliminates leading and trailing spaces 
                                    String[] aKeywords = line.split(" ");

                                    List<String> aListKeywords = new ArrayList<String>();
                                    aListKeywords= getCompoundNouns(aKeywords); 

                                    for(String s:aListKeywords)
                                    {
                                        if(!"".equals(timestamp))
                                        {
                                            if(containsName(concepts, s))
                                            {
                                                for(Concept c:concepts)
                                                {
                                                  String cn = c.getName();
                                                  if(cn.equals(s))
                                                  {
                                                      if(c.getTimedVideos().keySet().contains(id))
                                                      {
                                                          List<String> newList = new ArrayList<>();
                                                          newList = c.getTimedVideos().get(id);
                                                          newList.add(timestamp);
                                                          c.updateTimedVideos(id, newList);
                                                      }
                                                      else
                                                      {
                                                          List<String> newList = new ArrayList<>();
                                                          newList.add(timestamp);
                                                          c.updateTimedVideos(id, newList);
                                                      }
                                                  }
                                                }
                                            }
                                            else
                                            {
                                                Concept newConcept = new Concept();
                                                newConcept.setName(s);
                                                List<String> newList = new ArrayList<>();
                                                Map<String, List<String>> newMap = new HashMap<>();
                                                newList.add(timestamp);
                                                newMap.put(id, newList);
                                                newConcept.setTimedVideos(newMap);
                                                concepts.add(newConcept);  
                                            }
                                        }
                                    } 
                                }
                            }
                        }
                    }  
                }
            }
            catch(Exception ex)
            {
            }
        }
        return concepts;
    }
    
    public boolean containsName(final List<Concept> list, final String name)
    {
        return list.stream().filter(o -> o.getName().equals(name)).findFirst().isPresent();
    }    
    
    public Concept processKeyword(String keyword)
    {
        List<Concept> conceptList = new ArrayList<Concept>();
        conceptList =  GetKeywords();
        if(!conceptList.isEmpty())
        {
            for(Concept c:conceptList)
            {
                if(c.getName().equals(keyword))
                {
                    return c;
                }              
            } 
        }
        return null;
    }
    
    public List<String> getDataSet()
    {
        List<String> dataSet = new ArrayList<String>();

          dataSet.add("https://www.youtube.com/watch?v=x7qPAY9JqE4");
          dataSet.add("https://www.youtube.com/watch?v=r7fVWjgxRwk");
          dataSet.add("https://www.youtube.com/watch?v=wn3OCIcE8ds");
          dataSet.add("https://www.youtube.com/watch?v=82dwZYw2M00");
          dataSet.add("https://www.youtube.com/watch?v=0T2HCbv9FBQ");
          dataSet.add("https://www.youtube.com/watch?v=WV1LIvBnC9Q");
          dataSet.add("https://www.youtube.com/watch?v=2LaH8hyTLmM");
          dataSet.add("https://www.youtube.com/watch?v=ct-mHwAb3g4");
          dataSet.add("https://www.youtube.com/watch?v=9m_K2Yg7wGQ");
          dataSet.add("https://www.youtube.com/watch?v=OBhYxj2SvRI");
          dataSet.add("https://www.youtube.com/watch?v=YJ5q8Wrkbdw");
          dataSet.add("https://www.youtube.com/watch?v=kN0SVBCJqLs");
          dataSet.add("https://www.youtube.com/watch?v=3dxwopXL3fs");
          dataSet.add("https://www.youtube.com/watch?v=7GRv-kv5XEg");
          dataSet.add("https://www.youtube.com/watch?v=6EiI5_-7liQ");
          dataSet.add("https://www.youtube.com/watch?v=I6JWqllbhXE");
          dataSet.add("https://www.youtube.com/watch?v=FynpBelGCNc");
          dataSet.add("https://www.youtube.com/watch?v=afNHG3jyPU4");
          dataSet.add("https://www.youtube.com/watch?v=hs1HoLs4SD0");
          dataSet.add("https://www.youtube.com/watch?v=1nIgu3ou0A4");
        return dataSet;
    }
    
    public List<String> getDescription()
    {
        List<String> descritptionList = new ArrayList<String>();
        descritptionList.add("Steve Jobs Introducing The iPhone At MacWorld 2007");  
        descritptionList.add("Apple WWDC 2008 - iPhone 3G Introduction"); 
        descritptionList.add("iPhone 4 unveiled"); 
        descritptionList.add("Apple Special Event 2012- iPhone 5 Introduction"); 
        descritptionList.add("Apple Special Event 2014 - iPhone 6 & iPhone 6 Plus Introduction");         
        descritptionList.add("Apple's iPhone 7 Event in 15 minutes");  
        descritptionList.add("Apple iPhone 8 and 8 Plus - Full Announcement From Apple's 2017 Keynote"); 
        descritptionList.add("Apple iPhone X event in 15 minutes"); 
        descritptionList.add("Introducing iPhone XS and iPhone XR - Apple"); 
        descritptionList.add("Apple iPad: Steve Jobs Keynote Jan 27 2010");         
        descritptionList.add("Introducing the new iPad Pro - Apple");  
        descritptionList.add("Apple Music Event 2001-The First Ever iPod Introduction"); 
        descritptionList.add("Macworld San Francisco 2004 -The iPod mini Introduction"); 
        descritptionList.add("Apple Music Special Event 2005-The iPod Nano Introduction"); 
        descritptionList.add("Introducing Apple Watch Series 4 - Apple");         
        descritptionList.add("Macworld San Francisco 2006-The MacBook Pro Introduction");  
        descritptionList.add("Apple Special Event - 2nd Generation MacBook Air Introduction"); 
        descritptionList.add("Apple Special Event 2015 - MacBook Introduction"); 
        descritptionList.add("Introducing the new MacBook Air - Apple"); 
        descritptionList.add("Apple Without Steve Jobs");
        return descritptionList;
    }
    
    public List<String> getVideoIds()
    {
        List<String> videoIds = new ArrayList<String>();
        List<String> dataSet = new ArrayList<String>();
        dataSet = getDataSet();
        
        for(String url: dataSet)
        {
            String pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
            Pattern compiledPattern = Pattern.compile(pattern);
            Matcher matcher = compiledPattern.matcher(url);

            if(matcher.find())
            {
                videoIds.add(matcher.group());
            }    
        }
        return videoIds;
    }
    
    public List<String> getCompoundNouns(String[] arg)
    {
        String[] compoundWords = {"Apple Macintosh","iPhone 2","iPhone 3G","iPhone 3Gs","iphone 4","iPhone 4S","iphone 5","iphone 5s","iphone 5c","iphone 6","iphone 6s","iphone 7","iphone 8","iphone X","apple watch","Mac II", "MacBook Air", "MacBook Pro", "iPod mini", "iPod nano", "Apple TV", "iPod Classic", "iPod Shuffle", "iPod Touch", "iPad 2", "iPad 3", "iPad 4", "iPad mini", "iPad Air", "iPad Pro", "Apple watch"};
        List<String> newWords = new ArrayList<String>();
        for(String str:compoundWords)
        {
            for(int i=0; i<arg.length-1; i++)
            {
                if(str.toLowerCase().equals(arg[i] + " " + arg[i+1]))
                {
                    if(!newWords.contains(str.toLowerCase()))
                    {
                        newWords.add(str.toLowerCase());                                 
                    }
                }
                else
                {
                    if(!newWords.contains(arg[i]))
                    {
                        newWords.add(arg[i]);                                 
                    }
                }
            }
        }
        
        for(String str:newWords)
        {
            System.out.println(str);
        }
        
        return newWords;
    }
}