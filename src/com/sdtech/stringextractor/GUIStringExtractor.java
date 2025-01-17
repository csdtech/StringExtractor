package com.sdtech.stringextractor;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Label;
import java.awt.CheckboxGroup;
import java.awt.Checkbox;

public class GUIStringExtractor extends Frame {
    
    public GUIStringExtractor(){
        setLayout(new BorderLayout());
        Label modeTitle = new Label("Extraction Mode:");
        CheckboxGroup modes = new CheckboxGroup();
        Checkbox single = new Checkbox("Single");
        Checkbox multiple = new Checkbox("Multiple");
        Checkbox recursive = new Checkbox("Recursive");
        
        
    }
}
