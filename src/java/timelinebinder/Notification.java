/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package timelinebinder;

/**
 *
 * @author alamz
 */
public class Notification {
    protected static final boolean DEBUG = true; // Enabled debug mode by default may be useful to troubleshoot
    
    private String message = "";
    private String errorMessage = "";
    private String statusMessage = "";
    
    public void setMessage(String message)
    {
        this.message = message;
    }
    
     public String getMessage()
    {
        return this.message;
    }
    
    public void setErrorMessage(String eMessage)
    {
        this.errorMessage = eMessage;
    }
    
     public String getErrorMessage()
    {
        return this.errorMessage;
    }
     
      public void setStatusMessage(String sMessage)
    {
        this.statusMessage = sMessage;
    }
    
     public String getStatusMessage()
    {
        return this.statusMessage;
    }
}


