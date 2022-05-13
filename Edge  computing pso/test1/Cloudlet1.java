//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package test1;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.core.CloudSim;

public class Cloudlet1 {
    private int userId;
    private long cloudletLength;
    private final long cloudletFileSize;
    private final long cloudletOutputSize;
    private int numberOfPes;
    private final int cloudletId;
    private int status;
    private DecimalFormat num;
    private double finishTime;
    private double execStartTime;
    private int reservationId;
    private final boolean record;
    private String newline;
    private StringBuffer history;
    private final List<Cloudlet1.Resource> resList;
    private int index;
    private int classType;
    private int netToS;
    public static final int CREATED = 0;
    public static final int READY = 1;
    public static final int QUEUED = 2;
    public static final int INEXEC = 3;
    public static final int SUCCESS = 4;
    public static final int FAILED = 5;
    public static final int CANCELED = 6;
    public static final int PAUSED = 7;
    public static final int RESUMED = 8;
    public static final int FAILED_RESOURCE_UNAVAILABLE = 9;
    protected int vmId;
    protected double costPerBw;
    protected double accumulatedBwCost;
    private UtilizationModel utilizationModelCpu;
    private UtilizationModel utilizationModelRam;
    private UtilizationModel utilizationModelBw;
    private List<String> requiredFiles;

    public Cloudlet1(int cloudletId, long cloudletLength, int pesNumber, long cloudletFileSize, long cloudletOutputSize, UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam, UtilizationModel utilizationModelBw) {
        this(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu, utilizationModelRam, utilizationModelBw, false);
        this.vmId = -1;
        this.accumulatedBwCost = 0.0D;
        this.costPerBw = 0.0D;
        this.requiredFiles = new LinkedList();
    }

    public Cloudlet1(int cloudletId, long cloudletLength, int pesNumber, long cloudletFileSize, long cloudletOutputSize, UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam, UtilizationModel utilizationModelBw, boolean record, List<String> fileList) {
        this(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu, utilizationModelRam, utilizationModelBw, record);
        this.vmId = -1;
        this.accumulatedBwCost = 0.0D;
        this.costPerBw = 0.0D;
        this.requiredFiles = fileList;
    }

    public Cloudlet1(int cloudletId, long cloudletLength, int pesNumber, long cloudletFileSize, long cloudletOutputSize, UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam, UtilizationModel utilizationModelBw, List<String> fileList) {
        this(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu, utilizationModelRam, utilizationModelBw, false);
        this.vmId = -1;
        this.accumulatedBwCost = 0.0D;
        this.costPerBw = 0.0D;
        this.requiredFiles = fileList;
    }

    public Cloudlet1(int cloudletId, long cloudletLength, int pesNumber, long cloudletFileSize, long cloudletOutputSize, UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam, UtilizationModel utilizationModelBw, boolean record) {
        this.reservationId = -1;
        this.requiredFiles = null;
        this.userId = -1;
        this.status = 0;
        this.cloudletId = cloudletId;
        this.numberOfPes = pesNumber;
        this.execStartTime = 0.0D;
        this.finishTime = -1.0D;
        this.classType = 0;
        this.netToS = 0;
        this.cloudletLength = Math.max(1L, cloudletLength);
        this.cloudletFileSize = Math.max(1L, cloudletFileSize);
        this.cloudletOutputSize = Math.max(1L, cloudletOutputSize);
        this.resList = new ArrayList(2);
        this.index = -1;
        this.record = record;
        this.vmId = -1;
        this.accumulatedBwCost = 0.0D;
        this.costPerBw = 0.0D;
        this.requiredFiles = new LinkedList();
        this.setUtilizationModelCpu(utilizationModelCpu);
        this.setUtilizationModelRam(utilizationModelRam);
        this.setUtilizationModelBw(utilizationModelBw);
    }

    public boolean setReservationId(int resId) {
        if (resId <= 0) {
            return false;
        } else {
            this.reservationId = resId;
            return true;
        }
    }

    public int getReservationId() {
        return this.reservationId;
    }

    public boolean hasReserved() {
        return this.reservationId != -1;
    }

    public boolean setCloudletLength(long cloudletLength) {
        if (cloudletLength <= 0L) {
            return false;
        } else {
            this.cloudletLength = cloudletLength;
            return true;
        }
    }

    public boolean setNetServiceLevel(int netServiceLevel) {
        boolean success = false;
        if (netServiceLevel > 0) {
            this.netToS = netServiceLevel;
            success = true;
        }

        return success;
    }

    public int getNetServiceLevel() {
        return this.netToS;
    }

    public double getWaitingTime() {
        if (this.index == -1) {
            return 0.0D;
        } else {
            double subTime = ((Cloudlet1.Resource)this.resList.get(this.index)).submissionTime;
            return this.execStartTime - subTime;
        }
    }

    public boolean setClassType(int classType) {
        boolean success = false;
        if (classType > 0) {
            this.classType = classType;
            success = true;
        }

        return success;
    }

    public int getClassType() {
        return this.classType;
    }

    public boolean setNumberOfPes(int numberOfPes) {
        if (numberOfPes > 0) {
            this.numberOfPes = numberOfPes;
            return true;
        } else {
            return false;
        }
    }

    public int getNumberOfPes() {
        return this.numberOfPes;
    }

    public String getCloudletHistory() {
        String msg = null;
        if (this.history == null) {
            msg = "No history is recorded for Cloudlet #" + this.cloudletId;
        } else {
            msg = this.history.toString();
        }

        return msg;
    }

    public long getCloudletFinishedSoFar() {
        if (this.index == -1) {
            return this.cloudletLength;
        } else {
            long finish = ((Cloudlet1.Resource)this.resList.get(this.index)).finishedSoFar;
            return finish > this.cloudletLength ? this.cloudletLength : finish;
        }
    }

    public boolean isFinished() {
        if (this.index == -1) {
            return false;
        } else {
            boolean completed = false;
            long finish = ((Cloudlet1.Resource)this.resList.get(this.index)).finishedSoFar;
            long result = this.cloudletLength - finish;
            if ((double)result <= 0.0D) {
                completed = true;
            }

            return completed;
        }
    }

    public void setCloudletFinishedSoFar(long length) {
        if (!((double)length < 0.0D) && this.index >= 0) {
            Cloudlet1.Resource res = (Cloudlet1.Resource)this.resList.get(this.index);
            res.finishedSoFar = length;
            if (this.record) {
                this.write("Sets the length's finished so far to " + length);
            }

        }
    }

    public void setUserId(int id) {
        this.userId = id;
        if (this.record) {
            this.write("Assigns the Cloudlet to " + CloudSim.getEntityName(id) + " (ID #" + id + ")");
        }

    }

    public int getUserId() {
        return this.userId;
    }

    public int getResourceId() {
        return this.index == -1 ? -1 : ((Cloudlet1.Resource)this.resList.get(this.index)).resourceId;
    }

    public long getCloudletFileSize() {
        return this.cloudletFileSize;
    }

    public long getCloudletOutputSize() {
        return this.cloudletOutputSize;
    }

    public void setResourceParameter(int resourceID, double cost) {
        Cloudlet1.Resource res = new Cloudlet1.Resource();
        res.resourceId = resourceID;
        res.costPerSec = cost;
        res.resourceName = CloudSim.getEntityName(resourceID);
        this.resList.add(res);
        if (this.index == -1 && this.record) {
            this.write("Allocates this Cloudlet to " + res.resourceName + " (ID #" + resourceID + ") with cost = $" + cost + "/sec");
        } else if (this.record) {
            int id = ((Cloudlet1.Resource)this.resList.get(this.index)).resourceId;
            String name = ((Cloudlet1.Resource)this.resList.get(this.index)).resourceName;
            this.write("Moves Cloudlet from " + name + " (ID #" + id + ") to " + res.resourceName + " (ID #" + resourceID + ") with cost = $" + cost + "/sec");
        }

        ++this.index;
    }

    public void setSubmissionTime(double clockTime) {
        if (!(clockTime < 0.0D) && this.index >= 0) {
            Cloudlet1.Resource res = (Cloudlet1.Resource)this.resList.get(this.index);
            res.submissionTime = clockTime;
            if (this.record) {
                this.write("Sets the submission time to " + this.num.format(clockTime));
            }

        }
    }

    public double getSubmissionTime() {
        return this.index == -1 ? 0.0D : ((Cloudlet1.Resource)this.resList.get(this.index)).submissionTime;
    }

    public void setExecStartTime(double clockTime) {
        this.execStartTime = clockTime;
        if (this.record) {
            this.write("Sets the execution start time to " + this.num.format(clockTime));
        }

    }

    public double getExecStartTime() {
        return this.execStartTime;
    }

    public void setExecParam(double wallTime, double actualTime) {
        if (!(wallTime < 0.0D) && !(actualTime < 0.0D) && this.index >= 0) {
            Cloudlet1.Resource res = (Cloudlet1.Resource)this.resList.get(this.index);
            res.wallClockTime = wallTime;
            res.actualCPUTime = actualTime;
            if (this.record) {
                this.write("Sets the wall clock time to " + this.num.format(wallTime) + " and the actual CPU time to " + this.num.format(actualTime));
            }

        }
    }

    public void setCloudletStatus(int newStatus) throws Exception {
        if (this.status != newStatus) {
            if (newStatus >= 0 && newStatus <= 9) {
                if (newStatus == 4) {
                    this.finishTime = CloudSim.clock();
                }

                if (this.record) {
                    this.write("Sets Cloudlet status from " + this.getCloudletStatusString() + " to " + getStatusString(newStatus));
                }

                this.status = newStatus;
            } else {
                throw new Exception("Cloudlet.setCloudletStatus() : Error - Invalid integer range for Cloudlet status.");
            }
        }
    }

    public int getCloudletStatus() {
        return this.status;
    }

    public String getCloudletStatusString() {
        return getStatusString(this.status);
    }

    public static String getStatusString(int status) {
        String statusString = null;
        switch(status) {
            case 0:
                statusString = "Created";
                break;
            case 1:
                statusString = "Ready";
                break;
            case 2:
                statusString = "Queued";
                break;
            case 3:
                statusString = "InExec";
                break;
            case 4:
                statusString = "Success";
                break;
            case 5:
                statusString = "Failed";
                break;
            case 6:
                statusString = "Canceled";
                break;
            case 7:
                statusString = "Paused";
                break;
            case 8:
                statusString = "Resumed";
                break;
            case 9:
                statusString = "Failed_resource_unavailable";
        }

        return statusString;
    }

    public long getCloudletLength() {
        return this.cloudletLength;
    }

    public long getCloudletTotalLength() {
        return this.getCloudletLength() * (long)this.getNumberOfPes();
    }

    public double getCostPerSec() {
        return this.index == -1 ? 0.0D : ((Cloudlet1.Resource)this.resList.get(this.index)).costPerSec;
    }

    public double getWallClockTime() {
        return this.index == -1 ? 0.0D : ((Cloudlet1.Resource)this.resList.get(this.index)).wallClockTime;
    }

    public String[] getAllResourceName() {
        int size = this.resList.size();
        String[] data = null;
        if (size > 0) {
            data = new String[size];

            for(int i = 0; i < size; ++i) {
                data[i] = ((Cloudlet1.Resource)this.resList.get(i)).resourceName;
            }
        }

        return data;
    }

    public int[] getAllResourceId() {
        int size = this.resList.size();
        int[] data = null;
        if (size > 0) {
            data = new int[size];

            for(int i = 0; i < size; ++i) {
                data[i] = ((Cloudlet1.Resource)this.resList.get(i)).resourceId;
            }
        }

        return data;
    }

    public double getActualCPUTime(int resId) {
        Cloudlet1.Resource resource = this.getResourceById(resId);
        return resource != null ? resource.actualCPUTime : 0.0D;
    }

    public double getCostPerSec(int resId) {
        Cloudlet1.Resource resource = this.getResourceById(resId);
        return resource != null ? resource.costPerSec : 0.0D;
    }

    public long getCloudletFinishedSoFar(int resId) {
        Cloudlet1.Resource resource = this.getResourceById(resId);
        return resource != null ? resource.finishedSoFar : 0L;
    }

    public double getSubmissionTime(int resId) {
        Cloudlet1.Resource resource = this.getResourceById(resId);
        return resource != null ? resource.submissionTime : 0.0D;
    }

    public double getWallClockTime(int resId) {
        Cloudlet1.Resource resource = this.getResourceById(resId);
        return resource != null ? resource.wallClockTime : 0.0D;
    }

    public String getResourceName(int resId) {
        Cloudlet1.Resource resource = this.getResourceById(resId);
        return resource != null ? resource.resourceName : null;
    }

    public Cloudlet1.Resource getResourceById(int resourceId) {
        Iterator i$ = this.resList.iterator();

        Cloudlet1.Resource resource;
        do {
            if (!i$.hasNext()) {
                return null;
            }

            resource = (Cloudlet1.Resource)i$.next();
        } while(resource.resourceId != resourceId);

        return resource;
    }

    public double getFinishTime() {
        return this.finishTime;
    }

    protected void write(String str) {
        if (this.record) {
            if (this.num == null || this.history == null) {
                this.newline = System.getProperty("line.separator");
                this.num = new DecimalFormat("#0.00#");
                this.history = new StringBuffer(1000);
                this.history.append("Time below denotes the simulation time.");
                this.history.append(System.getProperty("line.separator"));
                this.history.append("Time (sec)       Description Cloudlet #" + this.cloudletId);
                this.history.append(System.getProperty("line.separator"));
                this.history.append("------------------------------------------");
                this.history.append(System.getProperty("line.separator"));
                this.history.append(this.num.format(CloudSim.clock()));
                this.history.append("   Creates Cloudlet ID #" + this.cloudletId);
                this.history.append(System.getProperty("line.separator"));
            }

            this.history.append(this.num.format(CloudSim.clock()));
            this.history.append("   " + str + this.newline);
        }
    }

    public int getStatus() {
        return this.getCloudletStatus();
    }

    public int getCloudletId() {
        return this.cloudletId;
    }

    public int getVmId() {
        return this.vmId;
    }

    public void setVmId(int vmId) {
        this.vmId = vmId;
    }

    public double getActualCPUTime() {
        return this.getFinishTime() - this.getExecStartTime();
    }

    public void setResourceParameter(int resourceID, double costPerCPU, double costPerBw) {
        this.setResourceParameter(resourceID, costPerCPU);
        this.costPerBw = costPerBw;
        this.accumulatedBwCost = costPerBw * (double)this.getCloudletFileSize();
    }

    public double getProcessingCost() {
        double cost = 0.0D;
        cost += this.accumulatedBwCost;
        cost += this.costPerBw * (double)this.getCloudletOutputSize();
        return cost;
    }

    public List<String> getRequiredFiles() {
        return this.requiredFiles;
    }

    protected void setRequiredFiles(List<String> requiredFiles) {
        this.requiredFiles = requiredFiles;
    }

    public boolean addRequiredFile(String fileName) {
        if (this.getRequiredFiles() == null) {
            this.setRequiredFiles(new LinkedList());
        }

        boolean result = false;

        for(int i = 0; i < this.getRequiredFiles().size(); ++i) {
            String temp = (String)this.getRequiredFiles().get(i);
            if (temp.equals(fileName)) {
                result = true;
                break;
            }
        }

        if (!result) {
            this.getRequiredFiles().add(fileName);
        }

        return result;
    }

    public boolean deleteRequiredFile(String filename) {
        boolean result = false;
        if (this.getRequiredFiles() == null) {
            return result;
        } else {
            for(int i = 0; i < this.getRequiredFiles().size(); ++i) {
                String temp = (String)this.getRequiredFiles().get(i);
                if (temp.equals(filename)) {
                    this.getRequiredFiles().remove(i);
                    result = true;
                    break;
                }
            }

            return result;
        }
    }

    public boolean requiresFiles() {
        boolean result = false;
        if (this.getRequiredFiles() != null && this.getRequiredFiles().size() > 0) {
            result = true;
        }

        return result;
    }

    public UtilizationModel getUtilizationModelCpu() {
        return this.utilizationModelCpu;
    }

    public void setUtilizationModelCpu(UtilizationModel utilizationModelCpu) {
        this.utilizationModelCpu = utilizationModelCpu;
    }

    public UtilizationModel getUtilizationModelRam() {
        return this.utilizationModelRam;
    }

    public void setUtilizationModelRam(UtilizationModel utilizationModelRam) {
        this.utilizationModelRam = utilizationModelRam;
    }

    public UtilizationModel getUtilizationModelBw() {
        return this.utilizationModelBw;
    }

    public void setUtilizationModelBw(UtilizationModel utilizationModelBw) {
        this.utilizationModelBw = utilizationModelBw;
    }

    public double getUtilizationOfCpu(double time) {
        return this.getUtilizationModelCpu().getUtilization(time);
    }

    public double getUtilizationOfRam(double time) {
        return this.getUtilizationModelRam().getUtilization(time);
    }

    public double getUtilizationOfBw(double time) {
        return this.getUtilizationModelBw().getUtilization(time);
    }

    private static class Resource {
        public double submissionTime;
        public double wallClockTime;
        public double actualCPUTime;
        public double costPerSec;
        public long finishedSoFar;
        public int resourceId;
        public String resourceName;

        private Resource() {
            this.submissionTime = 0.0D;
            this.wallClockTime = 0.0D;
            this.actualCPUTime = 0.0D;
            this.costPerSec = 0.0D;
            this.finishedSoFar = 0L;
            this.resourceId = -1;
            this.resourceName = null;
        }
    }
}
