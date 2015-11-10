package com.epiinfo.droid;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.os.Environment;
import android.util.Log;

import com.epiinfo.interpreter.Rule_Context;
import com.epiinfo.unc.Constants;

public class FormMetadata {

	public static LinkedList<Field> Fields;
	public static LinkedList<Field> DataFields;
	public static LinkedList<Field> NumericFields;
	public static LinkedList<Field> BooleanFields;
	public static LinkedList<Field> TextFields;
	public static int Height;
	public static int Width;
	public static String CheckCode;
	public static Rule_Context Context;
	public static int PageCount;
	public static String[] PageName;
	
	public FormMetadata(String viewXmlFile)
	{
		try
		{
			Context = null;
			Fields = new LinkedList<Field>();
			DataFields = new LinkedList<Field>();
			NumericFields = new LinkedList<Field>();
			TextFields = new LinkedList<Field>();
			BooleanFields = new LinkedList<Field>();
			CheckCode = "";
			File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			File file = new File(path, viewXmlFile);        		
		
			InputStream obj_is = null; 
			Document obj_doc = null; 
			DocumentBuilderFactory doc_build_fact = null; 
			DocumentBuilder doc_builder = null; 
			obj_is = new FileInputStream(file); 
			doc_build_fact = DocumentBuilderFactory.newInstance(); 
			doc_builder = doc_build_fact.newDocumentBuilder(); 

			obj_doc = doc_builder.parse(obj_is); 
			NodeList obj_nod_list = null; 
			if(null != obj_doc) 
			{ 
				Element feed = obj_doc.getDocumentElement();
				
				// v0.9.59
				if (feed == null) Log.d(Constants.LOGTAG, " " + "Element feed is null !!! ***************************");
				
				NodeList obj_view_list = feed.getElementsByTagName("View");
				
				// v0.9.59
				if (obj_view_list == null) Log.d(Constants.LOGTAG, " " + "NodeList obj_view_list is null !!! ***************************");
				
				Height = (int)Math.round(Integer.parseInt(obj_view_list.item(0).getAttributes().getNamedItem("Height").getNodeValue()) * DeviceManager.GetPageFactor());
				Width = (int)Math.round(Integer.parseInt(obj_view_list.item(0).getAttributes().getNamedItem("Width").getNodeValue()) * DeviceManager.GetPageFactor());
				CheckCode = obj_view_list.item(0).getAttributes().getNamedItem("CheckCode").getNodeValue().replace("://", "::").replaceAll("(?s)(/\\*{1})(.*)(\\*/{1})", "").replaceAll("(//{1})(.*)", "");
				
				// V0.9.59 - TEMP CODE !!!!!!!!
				// Height = 300; Width = 50;  CheckCode = "a";
				
				
				NodeList page_list = feed.getElementsByTagName("Page");
				PageCount = page_list.getLength();
				PageName = new String[PageCount];
				
				for (int i=0; i<PageCount; i++)
				{
					int pagePosition = Integer.parseInt(page_list.item(i).getAttributes().getNamedItem("Position").getNodeValue());
					int pageId = Integer.parseInt(page_list.item(i).getAttributes().getNamedItem("PageId").getNodeValue());
					obj_nod_list = page_list.item(i).getChildNodes();
				
					PageName[pagePosition] = page_list.item(i).getAttributes().getNamedItem("Name").getNodeValue();
					for (int x=0;x<obj_nod_list.getLength();x++)
					{
						NamedNodeMap test = obj_nod_list.item(x).getAttributes();
						if (test != null)
						{
							String fieldName = obj_nod_list.item(x).getAttributes().getNamedItem("Name").getNodeValue();
							String prompt = obj_nod_list.item(x).getAttributes().getNamedItem("PromptText").getNodeValue();
							String fieldType = obj_nod_list.item(x).getAttributes().getNamedItem("FieldTypeId").getNodeValue();
							double fieldX = Double.parseDouble(obj_nod_list.item(x).getAttributes().getNamedItem("ControlLeftPositionPercentage").getNodeValue());
							double fieldY = Double.parseDouble(obj_nod_list.item(x).getAttributes().getNamedItem("ControlTopPositionPercentage").getNodeValue());
							double fieldHeight = Double.parseDouble(obj_nod_list.item(x).getAttributes().getNamedItem("ControlHeightPercentage").getNodeValue());
							double fieldWidth = Double.parseDouble(obj_nod_list.item(x).getAttributes().getNamedItem("ControlWidthPercentage").getNodeValue());
							double controlFontSize = Double.parseDouble(obj_nod_list.item(x).getAttributes().getNamedItem("ControlFontSize").getNodeValue()) * DeviceManager.GetFontFactor();
							String controlFontStyle = obj_nod_list.item(x).getAttributes().getNamedItem("ControlFontStyle").getNodeValue();
							String pattern = obj_nod_list.item(x).getAttributes().getNamedItem("Pattern").getNodeValue();

							double lower = Double.MIN_VALUE;
							double upper = Double.MAX_VALUE;
							
							try
							{
								String lowerText = obj_nod_list.item(x).getAttributes().getNamedItem("Lower").getNodeValue();
								lower = Double.MIN_VALUE;
								if (!lowerText.equals(""))
								{
									lower = Double.parseDouble(lowerText);
								}
								String upperText = obj_nod_list.item(x).getAttributes().getNamedItem("Upper").getNodeValue();
								upper = Double.MAX_VALUE;
								if (!upperText.equals(""))
								{
									upper = Double.parseDouble(upperText);
								}
							}
							catch (Exception ex)
							{
								
							}
							
							double promptFontSize = 0;
							double promptX = 0;
							double promptY = 0;
							boolean isRequired = false;
							boolean isReadOnly = false;
							try
							{
								promptFontSize = Double.parseDouble(obj_nod_list.item(x).getAttributes().getNamedItem("PromptFontSize").getNodeValue()) * DeviceManager.GetFontFactor();
								promptX = Double.parseDouble(obj_nod_list.item(x).getAttributes().getNamedItem("PromptLeftPositionPercentage").getNodeValue());
								promptY = Double.parseDouble(obj_nod_list.item(x).getAttributes().getNamedItem("PromptTopPositionPercentage").getNodeValue());
								isRequired = obj_nod_list.item(x).getAttributes().getNamedItem("IsRequired").getNodeValue().equals("True") ? true : false;
								isReadOnly = obj_nod_list.item(x).getAttributes().getNamedItem("IsReadOnly").getNodeValue().equals("True") ? true : false;
							}
							catch (Exception ex)
							{
								// 
							}
							int maxLength = 0;
							try
							{
								maxLength = Integer.parseInt(obj_nod_list.item(x).getAttributes().getNamedItem("MaxLength").getNodeValue());
							}
							catch (Exception ex)
							{
								//
							}
					
							Field field = new Field(fieldName, prompt, fieldType, fieldX, fieldY, promptX, promptY, fieldWidth, fieldHeight, controlFontSize, controlFontStyle, promptFontSize, pagePosition, isRequired, isReadOnly, maxLength, lower, upper, pattern, pageId); 
							if (field.getType().equals("17"))
							{
								AddListValues(field, feed, obj_nod_list.item(x).getAttributes().getNamedItem("TextColumnName").getNodeValue());
							}
							if (field.getType().equals("19"))
							{
								AddListValues(field, feed, obj_nod_list.item(x).getAttributes().getNamedItem("TextColumnName").getNodeValue());
							}
							if (field.getType().equals("12"))
							{
								AddListValues(field, obj_nod_list.item(x).getAttributes().getNamedItem("List").getNodeValue());
							}
							Fields.add(field);
							if (!field.getType().equals("2") && !field.getType().equals("21") && !field.getType().equals("13"))
							{
								DataFields.add(field);
							}
							if (field.getType().equals("5"))
							{
								NumericFields.add(field);
							}
							if (field.getType().equals("1"))
							{
								TextFields.add(field);
							}
							if (field.getType().equals("10") || field.getType().equals("11"))
							{
								BooleanFields.add(field);
							}
						}
					}
				}
			}
			else {
				Log.d(Constants.LOGTAG, " " + "obj_doc is null !!! ***************************");
			}
		}
		catch (Exception ex) {
			Log.d(Constants.LOGTAG, " " + "FormMetadata Exception !!! ***************************");
			Log.d("FormMetadata", " Exception", ex);
		}
	}
	
	public static int GetFieldType(String fieldName)
	{
		for (int x = 0; x < Fields.size(); x++)
		{
			Field f = Fields.get(x);
			if (f.getName().toLowerCase().equals(fieldName.toLowerCase()))
			{
				return Integer.parseInt(f.getType());
			}
		}
		return 0;
	}
	
	public static Field GetFieldByName(String fieldName)
	{
		for (int x = 0; x < Fields.size(); x++)
		{
			Field f = Fields.get(x);
			if (f.getName().equalsIgnoreCase(fieldName))
			{
				return f;
			}
		}
		return null;
	}
	
	private void AddListValues(Field field, String list)
	{
		LinkedList<String> listValues = new LinkedList<String>();
		String[] step1 = list.split("\\|");
		String[] step2 = step1[0].split(",");
		for (int x=0;x<step2.length;x++)
		{
			listValues.add(step2[x]);
		}
		field.setListValues(listValues);
	}
	
	private void AddListValues(Field field, Element feed, String codeColumnName)
	{
		try
		{
			NodeList obj_nod_list = feed.getElementsByTagName("Item");
			LinkedList<String> listValues = new LinkedList<String>();
			listValues.add("<Not Selected>");
			boolean found = false;
			String currentParent = "";
			for (int x=0;x<obj_nod_list.getLength();x++)
			{
				Node node = obj_nod_list.item(x);
				Node attrib = node.getAttributes().item(0);
				String attribName = attrib.getNodeName().toLowerCase();
				if (attribName.equals(codeColumnName.toLowerCase()))
				{
					if (!currentParent.equals("") && !currentParent.equals(node.getParentNode().getAttributes().item(0).getNodeValue()))
						break;
					found = true;
					currentParent = node.getParentNode().getAttributes().item(0).getNodeValue();
					listValues.add(attrib.getNodeValue());
				}
				else
				{
					if (found)
						break;
				}
			}
			field.setListValues(listValues);
		}
		catch (Exception ex)
		{
			
		}
	}
	
	
}
