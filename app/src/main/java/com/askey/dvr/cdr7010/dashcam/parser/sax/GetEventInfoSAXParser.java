package com.askey.dvr.cdr7010.dashcam.parser.sax;

import com.askey.dvr.cdr7010.dashcam.domain.EventInfo;
import com.askey.dvr.cdr7010.dashcam.sax.GetEventInfoHandler;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class GetEventInfoSAXParser{
    public ArrayList<EventInfo> parser(InputStream in){
        if(in == null){
            return null;
        }
        InputSource xmlInputSource = new InputSource(in);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            GetEventInfoHandler defaultHandler = new GetEventInfoHandler();
            XMLReader reader;
            try{
                SAXParser parser = factory.newSAXParser();
                reader = parser.getXMLReader();
                reader.setContentHandler(defaultHandler);
                reader.setErrorHandler(defaultHandler);
                reader.parse(xmlInputSource);
            }catch(Exception e){
                return null;
            }
            return defaultHandler.getEventList();
        }
}