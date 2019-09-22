/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package timelinebinder;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author alamz
 */
public class Concept {
    private String _concept;
    private Map<String, List<String>> _timedVideos;
    //private List<Map<String, List<String>>> _listTimedVideos;
    private Map<String, List<String>> testVideoMap;

    public String getName()
    {
        return _concept;
    }
    
    public void setName(String _concept)
    {
        this._concept= _concept;
    }
    
    public Map<String, List<String>> getTimedVideos()
    {
        return _timedVideos;
    }
    
     public void setTimedVideos(Map<String, List<String>> _timedVideos )
    {
        this._timedVideos = _timedVideos;
        //_listTimedVideos.add(_timedVideos);
    }
     
    public void updateTimedVideos(String key, List<String> value)
    {
        _timedVideos.put(key, value);
    }
    
//    public List<Map<String, List<String>>> getListTimedVideos()
//    {
//        return _listTimedVideos;
//    }
    
//    public Concept(String _concept)
//    {
//        this._concept = _concept;
//        _timedVideos = new HashMap<String, List<String>>();
//        //_listTimedVideos = new ArrayList <>();
//    }
    
     public Concept()
    {
        _timedVideos = new HashMap<String, List<String>>();
    }
        
    public void setData()
    {
        String title = "testTitle";
        List<String> testStartEndTimes = new ArrayList<String>();
        testVideoMap = new HashMap<String, List<String>>();
        
        testStartEndTimes.add("00:00:07,910 > 00:00:14,849");
        testStartEndTimes.add("00:00:11,820 > 00:00:19,019");
        testStartEndTimes.add("00:00:19,019 > 00:00:23,910");
        testStartEndTimes.add("00:00:26,789 > 00:00:32,340");
        testStartEndTimes.add("00:00:35,399 > 00:00:45,799");
        testVideoMap.put("z__jxoczNWc_0_en", testStartEndTimes);
        
        setName(_concept);
        setTimedVideos(testVideoMap);        
    }
    
    public void ShowData()
    {
        for(String _timedVideos: _timedVideos.keySet())
        {
            System.out.println( _timedVideos );
            System.out.println( "------------------" );
            List<String> startEndTimes = new ArrayList<String>();
            startEndTimes = testVideoMap.get(_timedVideos);
            
            for(String str:startEndTimes)
            {
                System.out.println(str);
            }            
        }
    }   
}
