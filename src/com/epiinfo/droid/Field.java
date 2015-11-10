package com.epiinfo.droid;

import java.util.LinkedList;

public class Field {

	private String fieldName;
	private String fieldType;
	private String prompt;
	private String fieldFontStyle;
	private String pattern;
	private LinkedList<String> listValues;
	private int id;
	private double x;
	private double y;
	private double promptX;
	private double promptY;
	private double fieldWidth;
	private double fieldHeight;
	private double fieldFontSize;
	private double promptFontSize;
	private int position;
	private int pageId;
	private boolean isRequired;
	private boolean isReadOnly;
	private int maxLength;
	private double lower;
	private double upper;

	public Field(String fieldName, String prompt, String fieldType, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, double fieldFontSize, String fieldFontStyle, double promptFontSize, int position, boolean isRequired, boolean isReadOnly, int maxLength, double lower, double upper, String pattern, int pageId)
	{
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.prompt = prompt;
		this.x = x;
		this.y = y;
		this.promptX = promptX;
		this.promptY = promptY;
		this.fieldWidth = fieldWidth;
		this.fieldHeight = fieldHeight;
		this.fieldFontSize = fieldFontSize;
		this.fieldFontStyle = fieldFontStyle;
		this.promptFontSize = promptFontSize;
		this.position = position;
		this.pageId = pageId;
		this.isRequired = isRequired;
		this.isReadOnly = isReadOnly;
		this.maxLength = maxLength;
		this.lower = lower;
		this.upper = upper;
		this.pattern = pattern;
	}
	
	public void setListValues(LinkedList<String> listValues)
	{
		this.listValues=listValues;
	}
	
	public LinkedList<String> getListValues()
	{
		return listValues;
	}
	
	public String getName()
	{
		return fieldName;
	}
	
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	public String getType()
	{
		return fieldType;
	}
	
	public String getPrompt()
	{
		return prompt;
	}
	
	public double getX()
	{
		return x;
	}
	
	public double getY()
	{
		return y;
	}
	
	public double getPromptX()
	{
		return promptX;
	}
	
	public double getPromptY()
	{
		return promptY;
	}
	
	public double getFieldWidth()
	{
		return fieldWidth;
	}
	
	public double getFieldHeight()
	{
		return fieldHeight;
	}
	
	public double getFieldFontSize()
	{
		return fieldFontSize;
	}
	
	public String getFieldFontStyle()
	{
		return fieldFontStyle;
	}
	
	public double getPromptFontSize()
	{
		return promptFontSize;
	}
	
	public int getPagePosition()
	{
		return position;
	}
	
	public boolean getIsRequired()
	{
		return isRequired;
	}
	
	public boolean getIsReadOnly()
	{
		return isReadOnly;
	}
	
	public int getMaxLength()
	{
		return maxLength;
	}
	
	public double getLower()
	{
		return lower;
	}
	
	public double getUpper()
	{
		return upper;
	}
	
	public String getPattern()
	{
		return pattern;
	}
	
	public int getPageId()
	{
		return pageId;
	}
	
}
