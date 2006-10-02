/*
 * Created on Aug 19, 2004
 */
package org.cishell.reference.gui.persistence.save;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.cishell.framework.CIShellContext;
import org.cishell.framework.algorithm.AlgorithmProperty;
import org.cishell.framework.data.Data;
import org.cishell.framework.data.DataProperty;
import org.cishell.service.conversion.Converter;
import org.cishell.service.guibuilder.GUIBuilderService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * @author Team IVC
 */
public class FileSaver {
    private static File currentDir;

    private Shell parent;
    private LogService logService;
    private CIShellContext ciContext;
    
    private GUIBuilderService guiBuilder;


    public FileSaver(Shell parent, CIShellContext context){
        this.parent = parent;
        this.ciContext = context;
        this.logService = (LogService) ciContext.getService(LogService.class.getName());
        this.guiBuilder = (GUIBuilderService)context.getService(GUIBuilderService.class.getName());
    }       

    private boolean confirmFileOverwrite(File file) {
        String message = "The file:\n" + file.getPath()
            + "\nalready exists. Are you sure you want to overwrite it?";
        return guiBuilder.showConfirm("File Overwrite", message, "");
    }

    private boolean isSaveFileValid(File file) {
        boolean valid = false;
        if (file.isDirectory()) {
            String message = "Destination cannot be a directory. Please choose a file";
            guiBuilder.showError("Invalid Destination", message, "");
            valid = false;
        } else if (file.exists()) {
            valid = confirmFileOverwrite(file);
        }
        else
            valid = true ;
        return valid;
    }

    public boolean save(Converter converter, Data data) {
    	ServiceReference[] serviceReferenceArray = converter.getConverterChain();
    	String outDataStr = (String)serviceReferenceArray[serviceReferenceArray.length-1]
    	                                              .getProperty(AlgorithmProperty.OUT_DATA);

    	String ext = outDataStr.substring(outDataStr.indexOf(':')+1);
        
        if ((""+ext).startsWith(".")) {
            ext = ext.substring(1);
        }
        
        FileDialog dialog = new FileDialog(parent, SWT.SAVE);
        
        if (currentDir == null) {
            currentDir = new File(System.getProperty("user.home") + File.separator + "anything");
        }
        dialog.setFilterPath(currentDir.getPath());
        
        dialog.setFilterExtensions(new String[]{"*." + ext});
        dialog.setText("Choose File");
        
        String fileLabel = (String)data.getMetaData().get(DataProperty.LABEL);
        if (fileLabel == null) {
        	dialog.setFileName("*." + ext);
        } else {
        	dialog.setFileName(fileLabel + '.' + ext);        	
        }

        boolean done = false;
        
        while (!done) {        
            String fileName = dialog.open();
            if (fileName != null) {
                File selectedFile = new File(fileName);
                if (!isSaveFileValid(selectedFile))
                    continue;
                if (ext != null && ext.length() != 0)
                    if (!selectedFile.getPath().endsWith(ext))
                        selectedFile = new File(selectedFile.getPath()+'.'+ ext);

                Data newData = converter.convert(data);
                
                copy((File)newData.getData(), selectedFile);
                
                if (selectedFile.isDirectory()) {
                	currentDir = new File(selectedFile + File.separator + "anything");
                } else {
                	currentDir = new File(selectedFile.getParent() + File.separator + "anything");
                }
                    
                done = true;
       
                logService.log(LogService.LOG_INFO, "Saved: " + selectedFile.getPath() + "\n");
            } else {
                done = true;
                return false;
            }            
        }
        return true;
    }
    
    private boolean copy(File in, File out) {
    	try {
    		FileInputStream  fis = new FileInputStream(in);
    		FileOutputStream fos = new FileOutputStream(out);
    		
    		FileChannel readableChannel = fis.getChannel();
    		FileChannel writableChannel = fos.getChannel();
    		
    		writableChannel.truncate(0);
    		writableChannel.transferFrom(readableChannel, 0, readableChannel.size());
    		fis.close();
    		fos.close();
    		return true;
    	}
    	catch (IOException ioe) {
    		guiBuilder.showError("Copy Error", "IOException during copy", ioe.getMessage());
            return false;
    	}
    }
}