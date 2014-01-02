package com.example.whatstoday;



import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.io.IOException;
import java.util.Date;

public class Weather {
	public final static char UPDATE=0X01;
	public TextView status;
	public Weather(TextView pos)
	{
		status=pos;
		Thread bot = new Thread(new WeatherBot());
		bot.setDaemon(true);
		bot.start();
	}
	public Handler notice = new Handler(){ 
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what)
			{
				case UPDATE:
					Log.i("Update Receive","Received!!");
					if(msg.getData() instanceof Bundle){
						status.setText(((Bundle)msg.getData()).getString("xml", "No resource"));
					}
					if(msg.obj instanceof ArrayList)
					{
						StringBuffer temp=new StringBuffer();
						Iterator it=((ArrayList)msg.obj).iterator();
						while(it.hasNext())
						{
							Record current = (Record)it.next();
							temp.append(current.region+" : \n");
							
							for(Record.Measure m : current.measures)
							{
								if(m != null)
									for(Record.Time t : m.period)
									{
										temp.append("  "+t.start+"  "+t.end+"  ");
										if(t.isText)
											temp.append(t.text);
										else ; 
										
										if(t.cvalue != 100) 
											temp.append(" "+t.cvalue);
										else ;
										temp.append("\n");
									}
								else
									temp.append("Measure is NULL\n");
								temp.append("\n");
							}
							temp.append("\n");
						}
						status.setText(temp.toString());
					}
					else
						;
					break;
				default:
					break;
			}
			super.handleMessage(msg);
		}
		
	};
	class WeatherBot implements Runnable{
		final String URL="http://www.cwb.gov.tw/opendata/forecast/wf36hrC.xml";
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		private String header=null,xml_content=null;
		@Override
		public void run() {
			
			//for purpose of index of record.measures
			map.put("Wx", 0);
			map.put("MaxT", 1);
			map.put("MinT", 2);
			map.put("CI", 3);
			map.put("PoP", 4);
			ArrayList<Record> data=parseXML(URL); //parseXML(); // using http get
			Message msg = Message.obtain();
			msg.obj=data;
			msg.what=Weather.UPDATE;
			Weather.this.notice.sendMessage(msg);
		}
		public void parseXML()
		{
			// TODO Auto-generated method stub
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = null;
			try
			{
				HttpGet query = new HttpGet(URL);
				if((response=client.execute(query))==null){
					Log.e("Response Exception","Response is NULL!!");
				}
				else
				{
					for(Header head : response.getAllHeaders())
					{
						header += "\n"+head.toString();
					}
					xml_content=EntityUtils.toString(response.getEntity(),HTTP.UTF_8);
					Log.i("Success:GET XML","Success:GET XML");
					
					Message msg = Message.obtain();
					msg.what = Weather.UPDATE;
					Bundle bag = new Bundle();
					bag.putString("xml", xml_content);
					bag.putString("header", header);
					msg.setData(bag);
					Weather.this.notice.sendMessage(msg);
				}
			}
			catch(IllegalArgumentException ex)
			{
				Log.e("IllegalArgumentException", ex.getMessage());
			}
			catch(ClientProtocolException ex) {
				Log.e("ClientProtocolException", ex.getMessage());
			}
			catch(IOException ioe)
			{
				Log.e("IOException", ioe.getMessage());
			}
			catch(Exception e)
			{
				Log.e("Exception", e.getMessage());
			}
			finally
			{
				client.getConnectionManager().shutdown();
			}
		}
		@SuppressWarnings("finally")
		public ArrayList<Record> parseXML(String url)
		{
			ArrayList<Record> data = new ArrayList<Record>();
			try
			{
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				factory.setNamespaceAware(true);
				XmlPullParser xpp = factory.newPullParser();
				
				URL input = new URL(url);
				xpp.setInput(input.openStream(), null);

				int eventType = xpp.getEventType();
//				traversal all structures
				Record current= null;
				while( eventType != XmlPullParser.END_DOCUMENT ){
					switch(eventType)
					{
						case XmlPullParser.START_TAG:
							if("location".equalsIgnoreCase(xpp.getName())){
								data.add(new Record());
								current = data.get(data.size()-1);
								xpp.nextTag();
								xpp.require(XmlPullParser.START_TAG, null, "name");
								current.region=xpp.nextText();
//								xpp.nextTag();
								xpp.require(XmlPullParser.END_TAG, null, "name");
								
								xpp.nextTag(); //<weather-elements>
								saveXML(current,xpp,"Wx");
								saveXML(current,xpp,"MaxT");
								saveXML(current,xpp,"MinT");
								saveXML(current,xpp,"CI");
								saveXML(current,xpp,"PoP");
								xpp.nextTag(); //</weather-elements>
								xpp.nextTag(); //</location>
								xpp.require(XmlPullParser.END_TAG, null,"location");
							}
							else
								;
							break;
						default:
							break;
					}
					eventType = xpp.next();
				}
				
			}
			catch(XmlPullParserException xppe)
			{
				Log.e("XmlPullParserException "+xppe.getColumnNumber()+" "+xppe.getLineNumber(),xppe.getMessage());
			}
			catch(IOException e)
			{
				Log.e("IOException",e.getMessage());
			}
			finally
			{
				return data;
			}
		}
		private void saveXML(Record current,XmlPullParser xpp,final String TAG){
			try
			{
				xpp.nextTag(); // <Wx> <MaxT> <MinT> <CI> <PoP>
				xpp.require(XmlPullParser.START_TAG, null, TAG);
				String datetime;
				for(int index=0;xpp.nextTag()==XmlPullParser.START_TAG && xpp.getName().equalsIgnoreCase("time");index++){
					datetime=xpp.getAttributeValue(0); //format 2014-01-03T00:00:00+08:00
					current.measures[map.get(TAG)].period[index].start=Integer.parseInt(datetime.substring(0, 3)+datetime.substring(5, 6)+datetime.substring(8, 9)+datetime.substring(11, 12)); 
					//date+time ->int content = 2014010300
					datetime=xpp.getAttributeValue(1);
					current.measures[map.get(TAG)].period[index].end=Integer.parseInt(datetime.substring(0, 3)+datetime.substring(5, 6)+datetime.substring(8, 9)+datetime.substring(11, 12));
					//finish start & end
					if(TAG.equalsIgnoreCase("Wx") || TAG.equalsIgnoreCase("CI"))
					{
						xpp.nextTag();
						xpp.require(XmlPullParser.START_TAG, null, "text");
						current.measures[map.get(TAG)].period[index].text=xpp.nextText();
						current.measures[map.get(TAG)].period[index].isText=true;
//						xpp.nextTag(); // </text>
					}
					else ;
					if(TAG.equalsIgnoreCase("CI")) ;
					else
					{
						xpp.nextTag(); 
						xpp.require(XmlPullParser.START_TAG, null, "value");
						current.measures[map.get(TAG)].period[index].cvalue=Short.parseShort(xpp.nextText());
//						xpp.nextTag(); // </value>
					}
					xpp.nextTag(); // </time>
				};
//				xpp.nextTag(); // </wx> </MaxT> </MinT> </CI> </PoP> //for loop final round done it
				xpp.require(XmlPullParser.END_TAG, null, TAG);
				
			}
			catch(XmlPullParserException xppe)
			{
				Log.e("XmlPullParserException",xppe.getMessage());
			}
			catch(IOException ioe)
			{
				Log.e("IOException",ioe.getMessage());
			}
		}
	}
	class Record
	{
		public String region=null;
		public Measure[] measures = new Measure[5]; //replace public Time wx,maxt,mint,cl,pop;
		public Record(){
			for(int i=0;i<measures.length;i++)
				measures[i] = new Measure();
		}
		class Time
		{
			int start,end;
			boolean isText=false;
			String text=null;
			short cvalue=100;
		}
		class Measure
		{
			Time[] period = new Time[3];
			public Measure(){
				for(int i=0;i<period.length;i++)
					period[i] = new Time();
			}
		}
	}
}


