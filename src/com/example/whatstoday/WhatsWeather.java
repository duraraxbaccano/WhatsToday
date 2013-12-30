package com.example.whatstoday;

import java.util.ArrayList;

public class WhatsWeather implements Runnable{
	final String URL="http://www.cwb.gov.tw/opendata/forecast/wf36hrC.xml";
	ArrayList<Weather> weathers;
	public void run(){
		
	}
}
class Weather
{
	public String region=null,weather=null;
	public short temperature=0;
	
	public Weather(){}
	
}
