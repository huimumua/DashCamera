package com.askey.dvr.cdr7010.dashcam.domain;

import java.io.Serializable;

public class EventInfo implements Serializable{
    private static final long serialVersionUID = 1L;
    private int id = 0;
    private int priority = -1;
    private String eventName;
    private boolean isSupportPopUp;
    private boolean isSupportIcon;
    private boolean isSupportLed;
    private boolean isSupportSpeech;
    private String eventDescription;
    private String voiceGuidence;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }
    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }
    /**
     * @param priority the priority to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }
    /**
     * @return the eventName
     */
    public String getEventName() {
        return eventName;
    }
    /**
     * @param eventName the name to set
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    /**
     * @return the eventDescription
     */
    public String getEventDescription() {
        return eventDescription;
    }
    /**
     * @param eventDescription the eventDescription to set
     */
    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }
    /**
     * @return the voiceGuidence
     */
    public String getVoiceGuidence() {
        return voiceGuidence;
    }
    /**
     * @param voiceGuidence the voiceGuidence to set
     */
    public void setVoiceGuidence(String voiceGuidence) {
        this.voiceGuidence = voiceGuidence;
    }
    /**
     * @return the isSupportPopUp
     */
    public boolean isSupportPopUp() {
        return isSupportPopUp;
    }
    /**
     * @param isSupportPopUp the isSupportPopUp to set
     */
    public void setSupportPopUpStatus(boolean isSupportPopUp) {
        this.isSupportPopUp = isSupportPopUp;
    }
    /**
     * @return the isSupportIcon
     */
    public boolean isSupportIcon() {
        return isSupportIcon;
    }
    /**
     * @param isSupportIcon the isSupportIcon to set
     */
    public void setSupportIconStatus(boolean isSupportIcon) {
        this.isSupportIcon = isSupportIcon;
    }
    /**
     * @return the isSupportLed
     */
    public boolean isSupportLed() {
        return isSupportLed;
    }
    /**
     * @param isSupportLed the isSupportLed to set
     */
    public void setSupportLedStatus(boolean isSupportLed) {
        this.isSupportLed = isSupportLed;
    }
    /**
     * @return the isSupportSpeech
     */
    public boolean isSupportSpeech() {
        return isSupportSpeech;
    }
    /**
     * @param isSupportSpeech the isSupportSpeech to set
     */
    public void setSupportSpeechStatus(boolean isSupportSpeech) {
        this.isSupportSpeech = isSupportSpeech;
    }
    @Override
    public String toString(){
        return "EventInfo[ id="+id+",priority="+priority+",eventName="+eventName
                +",eventDescription="+eventDescription+",voiceGuidence="+voiceGuidence
                +",isSupportPopUp="+isSupportPopUp+",isSupportIcon="+isSupportIcon
                +",isSupportLed="+isSupportLed+",isSupportSpeech="+isSupportSpeech
                +"  ]";
    }

}