package com.askey.dvr.cdr7010.dashcam.sax;

import android.text.TextUtils;

import com.askey.dvr.cdr7010.dashcam.domain.EventInfo;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

public class GetEventInfoHandler extends DefaultHandler{
    private ArrayList<EventInfo> eventList;
    private EventInfo eventInfo;
    private String value ="";
    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        eventList = new ArrayList<>();
    }
    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(qName.equals("event")){
            eventInfo = new EventInfo();
        }
        super.startElement(uri, localName, qName, attributes);
    }
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equals("id")){
            try {
                eventInfo.setId(Integer.parseInt(value));
            }catch(Exception e){
                eventInfo.setId(0);
            }
        }
        if(qName.equals("priority")){
            try {
                eventInfo.setPriority(Integer.parseInt(value));
            }catch(Exception e){
                eventInfo.setPriority(Integer.MAX_VALUE);
            }
        }
        if(qName.equals("event_type")){
            try {
                eventInfo.setEventType(Integer.parseInt(value));
            }catch(Exception e){
                eventInfo.setEventType(-1);
            }
        }
        if(qName.equals("dialog_type")){
            try {
                eventInfo.setDialogType(Integer.parseInt(value));
            }catch(Exception e){
                eventInfo.setDialogType(-1);
            }
        }
        if(qName.equals("name")){
            eventInfo.setEventName(value);
        }
        if(qName.equals("description")){
            eventInfo.setEventDescription(value);
        }
        if(qName.equals("voice_guidence")){
            try {
                eventInfo.setVoiceGuidence(value);
            }catch(Exception e){
                eventInfo.setVoiceGuidence(null);
            }

        }
        if(qName.equals("popup")){
            if("yes".equalsIgnoreCase(value)){
                eventInfo.setSupportPopUpStatus(true);
            }else{
                eventInfo.setSupportPopUpStatus(false);
            }
        }
        if(qName.equals("icon")){
            if("yes".equalsIgnoreCase(value)){
                eventInfo.setSupportIconStatus(true);
            }else{
                eventInfo.setSupportIconStatus(false);
            }
        }
        if(qName.equals("led")){
            if("yes".equalsIgnoreCase(value)){
                eventInfo.setSupportLedStatus(true);
            }else{
                eventInfo.setSupportLedStatus(false);
            }
        }
        if(qName.equals("speech")){
            if("yes".equalsIgnoreCase(value)){
                eventInfo.setSupportSpeechStatus(true);
            }else{
                eventInfo.setSupportSpeechStatus(false);
            }
        }
        if(qName.equals("event")){
            eventList.add(eventInfo);
            eventInfo = null;
        }
        super.endElement(uri, localName, qName);
    }
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if(length > 0) {
            String str = new String(ch,0,length);
            if(!TextUtils.isEmpty(str) && ch[0] !='\n') {
                value = str;
            }
        }
        super.characters(ch, start, length);
    }
    public ArrayList<EventInfo> getEventList(){
        return eventList;
    }
}
