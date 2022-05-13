package test;

import java.io.File;  
import java.io.FileInputStream;  
import java.util.Iterator;  
import org.apache.poi.ss.usermodel.Cell;  
import org.apache.poi.ss.usermodel.Row;  
import org.apache.poi.xssf.usermodel.XSSFSheet;  
import org.apache.poi.xssf.usermodel.XSSFWorkbook;  


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;

public class FileReader 
{  
    private class Field {
        public int[] jobNum;
        public long[] submitTime;
        public long[] waitTime;
        public int[] runTime;
        public int[] numProc;
        public int[] reqNumProc;
        public int[] reqRunTime;
        public int[] userId; 
        public int[] groupId;
        public int[] precedingJob;
        public Field() {
            jobNum = new int[Constants.NoOfTasks];
            submitTime = new long[Constants.NoOfTasks];
            waitTime = new long[Constants.NoOfTasks];
            runTime = new int[Constants.NoOfTasks];
            numProc = new int[Constants.NoOfTasks];
            reqNumProc = new int[Constants.NoOfTasks];
            reqRunTime = new int[Constants.NoOfTasks];
            userId = new int[Constants.NoOfTasks];
            groupId = new int[Constants.NoOfTasks];
            precedingJob = new int[Constants.NoOfTasks]; 
        }
    }

    private Field field;
    private File file;
    private ArrayList<Cloudlet> jobs = null; // a list for getting all the

	// Gridlets

	// using Standard Workload Format
	private final int JOB_NUM = 1 - 1; // job number

	private final int SUBMIT_TIME = 2 - 1; // submit time of a Gridlet

    private final int WAIT_TIME = 3 - 1; // wait time of a Gridlet

	private final int RUN_TIME = 4 - 1; // running time of a Gridlet

	private final int NUM_PROC = 5 - 1; // number of processors needed for a

	// Gridlet
	private final int REQ_NUM_PROC = 8 - 1; // required number of processors

	private final int REQ_RUN_TIME = 9 - 1; // required running time

	private final int USER_ID = 12 - 1; // if of user who submitted the job

	private final int GROUP_ID = 13 - 1; // if of group of the user who

    private final int PRECEDING_JOB = 17 - 1; // id of preceding job

	//submitted the job
	private int MAX_FIELD = 18; // max number of field in the trace file

    public FileReader(final String fileName) throws FileNotFoundException {
        this.field = new Field();
        if (fileName == null || fileName.length() == 0) {
			throw new IllegalArgumentException("Invalid trace file name.");
        }
        this.file = new File(fileName);
		if (!file.exists()) {
			throw new FileNotFoundException("Workload trace " + fileName + " does not exist");
		}    
    }
        
    
    public boolean readFile() throws IOException, FileNotFoundException {
        boolean success = false;  
        try  
        {     
            FileInputStream fis = new FileInputStream(this.file);   //obtaining bytes from the file  
            //creating Workbook instance that refers to .xlsx file  
            XSSFWorkbook wb = new XSSFWorkbook(fis);   
            XSSFSheet sheet = wb.getSheetAt(0);     //creating a Sheet object to retrieve object  
            Iterator<Row> itr = sheet.iterator();    //iterating over excel file
            if (itr.hasNext()) itr.next(); //omit first row
            int rowIdx = 0;
            while (itr.hasNext() && rowIdx < Constants.NoOfTasks)                 
            {  
                Row row = itr.next();  
                Iterator<Cell> cellIterator = row.cellIterator();   //iterating over each column
                int colIdx = -1; //index of column cell   
                while (cellIterator.hasNext())   
                {  
                    Cell cell = cellIterator.next();  
                    colIdx++;
                    if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        switch (colIdx) {
                            case JOB_NUM:
                                field.jobNum[rowIdx] = (int)cell.getNumericCellValue();
                            case SUBMIT_TIME:
                                field.submitTime[rowIdx] = (long)cell.getNumericCellValue();
                            case WAIT_TIME:
                                field.waitTime[rowIdx] = (long)cell.getNumericCellValue();
                            case RUN_TIME:
                                field.runTime[rowIdx] = (int)cell.getNumericCellValue();
                            case NUM_PROC:
                                field.numProc[rowIdx] = (int)cell.getNumericCellValue();
                            case REQ_NUM_PROC:
                                field.reqNumProc[rowIdx] = (int)cell.getNumericCellValue();
                            case REQ_RUN_TIME:
                                field.reqRunTime[rowIdx] = (int)cell.getNumericCellValue();
                            case USER_ID:
                                field.userId[rowIdx] = (int)cell.getNumericCellValue();
                            case GROUP_ID:
                                field.groupId[rowIdx] = (int)cell.getNumericCellValue();
                            case PRECEDING_JOB:
                                field.precedingJob[rowIdx] = (int)cell.getNumericCellValue();
                            default:    
                
                        }
                    }
                      
                }  
                //System.out.println("");
                rowIdx++;  
            }
            success = true;
        }  
        catch(Exception e)  
        {  
            e.printStackTrace();  
        }  
        return success;       
    }  

    private void createJob(final int id, final long submitTime, final int runTime, 
			final int numProc, final int reqRunTime, final int userID, final int groupID) {
		final int len = runTime;
		
		UtilizationModel utilizationModel = new UtilizationModelFull();
		long fileSize = 300;
		long outputSize = 300;
		final Cloudlet wgl = new Cloudlet(id, len, numProc,	fileSize, outputSize, utilizationModel, 
				utilizationModel, utilizationModel);
		
		wgl.setUserId(userID);
		jobs.add(wgl);
	}

    public ArrayList<Cloudlet> generateWorkload() {
		if (jobs == null) {
			jobs = new ArrayList<Cloudlet>();
            for (int i = 0; i < Constants.NoOfTasks; i++) {
                createJob(field.jobNum[i], field.submitTime[i], field.runTime[i], field.numProc[i], field.reqRunTime[i], field.userId[i], field.groupId[i]);
            }
		}
		return jobs;
	}

    public int[] getRunTime() {
        return field.runTime;
    }

    public int[] getPrecedingJob() {
        return field.precedingJob;
    }

    public int[] getPesNumber() {
        return  field.numProc;
    }
}