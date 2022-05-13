package test1;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.*;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import test.Constants;
import test.MyDataCenterBroker;
import test.PriorityDatacenterBroker;
import test.SJF_DatacenterBroker;

public class TaskScheduler1 {

    /** The cloudlet list. */
    private static List<Cloudlet> cloudletList;

    /** The vmlist. */
    private static List<Vm> vmlist;
    /*
     * mapping: represents the mapping of cloudlets to vm based on MultiSwarmPSO mapping2: represents
     * the mapping of cloudlets to vm based on Random Scheduling resultcost: stores the cost of
     * cloudlet execution based on the mappings other parameters are defined in FitnessFunction
     * 映射：表示cloudlets到基于MultiWarmPSO的vm的映射   mapping2：表示基于随机调度的Cloudlet到vm的映射   resultcost：根据FitnessFunction中定义的映射存储cloudlet执行的成本
     */
    public static double mapping[];
    public static double[][] executiontimematrix;  //执行时间矩阵
    public static double[][] communicationtimematrix;  //通信时间矩阵
    public static double[][] datatransfermatrix;  //数据传输矩阵
    public static double[][] taskoutputfilematrix;   //任务输出文件矩阵
    public static double[][] commcost;  //通信成本
    public static double[] mapping2 = new double[Constants.NoOfTasks];
    public static int depgraph[][] = new int[Constants.NoOfTasks][Constants.NoOfTasks];  //分布图

    public static double[] resultcost = new double[6];  //结果成本

    /**
     * Creates main() to run this example
     */
    public double[] getPSOMapping() {
        return mapping;
    }

    // /*
    // * This function is used for the simulation of Cloud Scenarios using CloudSim.
    // */

    public static double[] func(int[] tasklength, int[] outputfilesize, int[] mips, double[] execcost,
                                double[] waitcost, int[][] graph, int[] pesNumber) throws Exception {
        /*
         * Depgraph denotes that a task requires output files from which tasks
         * Depgraph表示任务需要输出文件，这些文件来自
         */
        for (int j = 0; j < Constants.NoOfTasks; j++) {
            for (int k = 0; k < Constants.NoOfTasks; k++) {
                depgraph[k][j] = graph[j][k];
            }
        }
        /*
         * Run the PSO to obtain the mapping of cloudlets to VM
         * 运行PSO以获得cloudlets到VM的映射
         */
        PSO1 PSOScheduler = new PSO1(tasklength, outputfilesize, mips, execcost, waitcost, graph);   //得到粒子的速度和位置
        //得到最好的位置
        mapping = PSOScheduler.run();
        // for(int i=0;i<Constants.NoOfVMs;i++)
        // createvmrunscript(i);
        // createscript(mapping,"pso");
        executiontimematrix = PSOScheduler.getexecutiontimematrix();
        communicationtimematrix = PSOScheduler.getcommunicationtimematrix();
        datatransfermatrix = PSOScheduler.getdatatransfermatrix();
        commcost = PSOScheduler.getcommcost();
        taskoutputfilematrix = PSOScheduler.gettaskoutputfilematrix();

//正在启动TaskScheduler，其中有一个具有不同虚拟机的数据中心。。。
        Log.printLine(" Starting TaskScheduler having 1 datacenter with different VMs...");

        try {
            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            int num_user = 1; // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);

            // Second step: Create Datacenters
            // Datacenters are the resource providers in CloudSim. We need at list one of them to run a
            // CloudSim simulation
            @SuppressWarnings("unused")
            Datacenter datacenter0 = createDatacenter("Datacenter_0");

            // Third step: Create Broker
            MyDataCenterBroker broker = createBroker();

            int brokerId = broker.getId();


            // //submit vm list to the broker
            vmlist = createVm(mips, brokerId);
            broker.submitVmList(vmlist);

            // Fifth step: Create two Cloudlets
            cloudletList = new ArrayList<Cloudlet>();

            cloudletList = createCloudLets(tasklength, pesNumber, outputfilesize, brokerId);
            // submit cloudlet list to the broker
            broker.submitCloudletList(cloudletList);
            //延迟
            double delay[] = new double[Constants.NoOfTasks];
            delay[0] = 0;
            for (int i = 0; i < Constants.NoOfTasks; i++) {
                for (int j = i + 1; j < Constants.NoOfTasks; j++) {
                    if (taskoutputfilematrix[i][j] != 0) {
                        delay[j] = Math.max(delay[j], delay[i] + executiontimematrix[i][(int) mapping[i]]
                                + communicationtimematrix[i][j]);
                    }
                }
            }

            broker.submitMapping(mapping);
            broker.submitDelay(delay);

            // Sixth step: Starts the simulation
            CloudSim.startSimulation();

            // Final step: Print results when simulation is over
            List<Cloudlet> newList = broker.getCloudletReceivedList();



            CloudSim.stopSimulation();

            printCloudletList(newList);

            resultcost[0] = PSOScheduler.printBestFitness();

            Log.printLine("Simulation of Task Scheduler using PSO is finished!");
            //映射关系
        for (int i = 0; i < Constants.NoOfTasks; i++) {
        System.out.println(mapping[i]);

        System.out.println("----");
      }

      for (int i = 0; i < Constants.NoOfTasks; i++) {
        int vm = (int) mapping[i];  //mapping对应的整数就是分配虚拟机序号，
        System.out.print("" + executiontimematrix[i][vm] + "*" + execcost[vm] + "\n");
      }
            //最短作业优先
            SJFscheduler(executiontimematrix, communicationtimematrix, datatransfermatrix, commcost,
                    taskoutputfilematrix, tasklength, outputfilesize, mips, execcost, waitcost, graph,
                    pesNumber);
            //KPSO调度
            func_1( tasklength,  outputfilesize,  mips, execcost, waitcost,  graph,  pesNumber);
            //LPSO调度
            func_2( tasklength,  outputfilesize,  mips, execcost, waitcost,  graph,  pesNumber);
            //LKPSO调度
            func_3( tasklength,  outputfilesize,  mips, execcost, waitcost,  graph,  pesNumber);
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
        return resultcost;
    }

    public static void randomscheduler(double[][] executiontimematrix,
                                       double[][] communicationtimematrix, double[][] datatransfermatrix, double[][] commcost,
                                       double[][] taskoutputfilematrix, int[] tasklength, int[] outputfilesize, int[] mips,
                                       double[] execcost, double[] waitcost, int[][] graph, int[] pesNumber) throws Exception {
        double[] mapping2 = new double[Constants.NoOfTasks];

        Log.printLine(" Starting TaskScheduler having 1 datacenter with different VMs...");

        try {
            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            int num_user = 1; // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);

            // Second step: Create Datacenters
            // Datacenters are the resource providers in CloudSim. We need at list one of them to run a
            // CloudSim simulation
            @SuppressWarnings("unused")
            Datacenter datacenter0 = createDatacenter("Datacenter_0");

            // Third step: Create Broker
            MyDataCenterBroker broker = createBroker();
            int brokerId = broker.getId();

            vmlist = createVm(mips, brokerId);
            // submit vm list to the broker
            broker.submitVmList(vmlist);

            // Fifth step: Create two Cloudlets
            cloudletList = new ArrayList<Cloudlet>();

            // Cloudlet properties
            // This is the mips rating(Million Instructions per second which are processed) of each VM
            // that is being called
            // This is the cost of execution on different VM per unit time.

            UtilizationModel utilizationModel = new UtilizationModelFull();
            for (int i = 0; i < Constants.NoOfTasks; i++)
                mapping2[i] = (int) (Math.random() * (Constants.NoOfVMs));

            cloudletList = createCloudLets(tasklength, pesNumber, outputfilesize, brokerId);
            // submit cloudlet list to the broker

            broker.submitCloudletList(cloudletList);
            double delay2[] = new double[Constants.NoOfTasks];
            delay2[0] = 0;

            for (int i = 0; i < Constants.NoOfTasks; i++) {
                for (int j = i + 1; j < Constants.NoOfTasks; j++) {
                    if (taskoutputfilematrix[i][j] != 0) {
                        delay2[j] = Math.max(delay2[j], delay2[i] + executiontimematrix[i][(int) mapping2[i]]
                                + communicationtimematrix[i][j]);
                    }
                }
            }
            broker.submitMapping(mapping2);
            broker.submitDelay(delay2);
            // bind the cloudlets to the vms. This way, the broker
            // will submit the bound cloudlets only to the specific VM

            double cost = 0.0;
            double[] vmworkingcost = new double[Constants.NoOfVMs];

            double waiting = 0.0;
            for (int i = 0; i < Constants.NoOfTasks; i++) {
                int vm = (int) mapping2[i];
                vmworkingcost[vm] += (executiontimematrix[i][vm]) * execcost[vm];
                System.out.println("" + executiontimematrix[i][vm] + "*" + execcost[vm]);
            }
            for (int i = 0; i < Constants.NoOfTasks; i++) {
                int vm = (int) mapping2[i];
                for (int j = i + 1; j < Constants.NoOfTasks; j++) {
                    vmworkingcost[vm] += (communicationtimematrix[i][j]) * commcost[i][j];
                }
            }
            for (int i = 0; i < Constants.NoOfVMs; i++)
                cost += vmworkingcost[i];
            for (int i = 0; i < Constants.NoOfTasks; i++) {
                cost += delay2[i] * waitcost[(int) mapping2[i]];
            }

            System.out.println("the cost for random scheduling is " + cost);

            // Sixth step: Starts the simulation
            CloudSim.startSimulation();

            // Final step: Print results when simulation is over
            List<Cloudlet> newList2 = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

            printCloudletList(newList2);

            resultcost[1] = cost;
            Log.printLine("Simulation of Task Scheduler using Random scheduling is finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    /**
     * Implement this to use Collections.sort() on cloudlet
     *
     */
    public static class SortByCloudletLength implements Comparator<Cloudlet> {
        public int compare(Cloudlet a, Cloudlet b) {
            double as = a.getCloudletLength();
            double bs = b.getCloudletLength();
            if (as > bs) return 1;
            else if (as == bs) return 0;
            else return -1;
        }
    }


    public static void SJFscheduler(double[][] executiontimematrix,
                                    double[][] communicationtimematrix, double[][] datatransfermatrix, double[][] commcost,
                                    double[][] taskoutputfilematrix, int[] tasklength, int[] outputfilesize, int[] mips,
                                    double[] execcost, double[] waitcost, int[][] graph, int[] pesNumber) throws Exception {

        Log.printLine(" Starting TaskScheduler having 1 datacenter with different VMs...");

        try {
            int num_user = 1; // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events
            CloudSim.init(num_user, calendar, trace_flag);

            @SuppressWarnings("unused")
            Datacenter datacenter0 = createDatacenter("Datacenter_0");

            // Create custom broker
            SJF_DatacenterBroker broker = createSjfBroker();
            int brokerId = broker.getId();

            vmlist = createVm(mips, brokerId);
            cloudletList = createCloudLets(tasklength, pesNumber, outputfilesize, brokerId);
            //升序
            Collections.sort(cloudletList, new SortByCloudletLength());
            int[] mapping_sjf = new int[Constants.NoOfTasks];
            int index = 0;
            for(Cloudlet cloudlet : cloudletList) {
                mapping_sjf[cloudlet.getCloudletId()] = index % Constants.NoOfVMs;   //任务和虚拟机对应
                index++;
            }


//      for (int i = 0; i < Constants.NoOfTasks; i++)
//          mapping_sjf[i] = (int) (Math.random() * (Constants.NoOfVMs));
//				// mapping_sjf[i] = i % Constants.NoOfVMs;
            double delay_sjf[] = new double[Constants.NoOfTasks];
            delay_sjf[0] = 0;
            for (int i = 0; i < Constants.NoOfTasks; i++) {
                for (int j = i + 1; j < Constants.NoOfTasks; j++) {
                    if (taskoutputfilematrix[i][j] != 0) {   //任务传输的数据量
                        delay_sjf[j] = Math.max(
                                delay_sjf[j],
                                delay_sjf[i] + executiontimematrix[i][mapping_sjf[i]] + communicationtimematrix[i][j]  //执行时间+通信时间
                        );
                    }


                }
//        System.out.println(delay_sjf[i]);
            }
            broker.submitVmList(vmlist);
            broker.submitCloudletList(cloudletList);
            broker.submitMapping(mapping_sjf);
            broker.submitDelay(delay_sjf);

            double cost = 0.0;
            double[] vmworkingcost = new double[Constants.NoOfVMs];
            double waiting = 0.0;
            for (int i = 0; i < Constants.NoOfTasks; i++) {
                int vm = mapping_sjf[i];
                vmworkingcost[vm] += (executiontimematrix[i][vm]) * execcost[vm];
                System.out.println("" + executiontimematrix[i][vm] + "*" + execcost[vm]);//虚拟机执行成本

            }
//      for (int i=0;i<8;i++){
//        System.out.println(vmworkingcost[i]);
//      }
            for (int i = 0; i < Constants.NoOfTasks; i++) {
                int vm = mapping_sjf[i];
                for (int j = i + 1; j < Constants.NoOfTasks; j++) {
                    vmworkingcost[vm] += (communicationtimematrix[i][j]) * commcost[i][j]; //虚拟机通信成本
                }
            }
            for (int i = 0; i < Constants.NoOfVMs; i++)
                cost += vmworkingcost[i];
            for (int i = 0; i < Constants.NoOfTasks; i++) {
                cost += delay_sjf[i] * waitcost[mapping_sjf[i]];   //任务总延迟
            }
            System.out.println("The cost for SJF scheduling is " + cost);

            // Sixth step: Starts the simulation
            CloudSim.startSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

            printCloudletList4(newList);

            mapping_sjf = broker.getMapping();
            System.out.println("---Mapping---");
            for (int i = 0; i < Constants.NoOfTasks; i++) {
                System.out.print(mapping_sjf[i] + " ");
            }
            System.out.println("");

            resultcost[2] = cost;

            Log.printLine("Simulation of Task Scheduler using SJF scheduling is finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    //按Cloudlet优先级长度排序
    public static class SortByCloudletPriorityLength implements Comparator<Cloudlet> {
        public int compare(Cloudlet a, Cloudlet b) {
            double as = a.getCloudletLength();
            double bs = b.getCloudletLength();
            if (as < bs) return 1;
            else if (as == bs) return 0;
            else return -1;
        }
    }

    //优先级调度
    public static void priorityScheduler(double[][] executiontimematrix,
                                         double[][] communicationtimematrix, double[][] datatransfermatrix, double[][] commcost,
                                         double[][] taskoutputfilematrix, int[] tasklength, int[] outputfilesize, int[] mips,
                                         double[] execcost, double[] waitcost, int[][] graph, int[] pesNumber) throws Exception {

        Log.printLine(" PS  Starting TaskScheduler having 1 datacenter with different VMs...");

        try {
            int num_user = 1; // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events
            CloudSim.init(num_user, calendar, trace_flag);

            @SuppressWarnings("unused")
            Datacenter datacenter0 = createDatacenter("Datacenter_0");

            // Create custom broker
            PriorityDatacenterBroker broker = createPriorityBroker();
            int brokerId = broker.getId();

            vmlist = createVm(mips, brokerId);
            cloudletList = createCloudLets(tasklength, pesNumber, outputfilesize, brokerId);
            //按照任务优先级排序
            Collections.sort(cloudletList, new SortByCloudletPriorityLength());
            int[] mappingPriority = new int[Constants.NoOfTasks];
            int index = 0;
            for(Cloudlet cloudlet : cloudletList) {
                mappingPriority[cloudlet.getCloudletId()] = index % Constants.NoOfVMs;
                index++;
            }
            double makespan = 0.0;
            double[] vmworkingtime = new double[Constants.NoOfVMs];
            for (int k = 0; k<Constants.NoOfTasks; k++){
                int vm1 = mappingPriority[k];

                if(vmworkingtime[vm1] != 0) --vmworkingtime[vm1];
                vmworkingtime[vm1]+=executiontimematrix[k][vm1]
                        + communicationtimematrix[k][vm1];
                makespan=Math.max(makespan,vmworkingtime[vm1]);}
            System.out.println("The makespan for Priority scheduling is " + makespan);

      /*
      for (int i = 0; i < Constants.NoOfTasks; i++)
          mapping_sjf[i] = (int) (Math.random() * (Constants.NoOfVMs));
					// mapping_sjf[i] = i % Constants.NoOfVMs;

			*/
            //延迟
            double delayPriority[] = new double[Constants.NoOfTasks];
            delayPriority[0] = 0;
            for (int i = 0; i < Constants.NoOfTasks; i++) {
                for (int j = i + 1; j < Constants.NoOfTasks; j++) {
                    if (taskoutputfilematrix[i][j] != 0) {
                        delayPriority[j] = Math.max(
                                delayPriority[j],
                                delayPriority[i] + executiontimematrix[i][mappingPriority[i]] + communicationtimematrix[i][j]
                        );
                    }
                }
            }
            broker.submitVmList(vmlist);
            broker.submitCloudletList(cloudletList);
            broker.submitMapping(mappingPriority);
            broker.submitDelay(delayPriority);



            double cost = 0.0;
            double[] vmworkingcost = new double[Constants.NoOfVMs];
            double waiting = 0.0;
            for (int i = 0; i < Constants.NoOfTasks; i++) {
                int vm = mappingPriority[i];
                vmworkingcost[vm] += (executiontimematrix[i][vm]) * execcost[vm];
                System.out.println("" + executiontimematrix[i][vm] + "*" + execcost[vm]);
            }
            for (int i = 0; i < Constants.NoOfTasks; i++) {
                int vm = mappingPriority[i];
                for (int j = i + 1; j < Constants.NoOfTasks; j++) {
                    vmworkingcost[vm] += (communicationtimematrix[i][j]) * commcost[i][j];
                }
            }
            for (int i = 0; i < Constants.NoOfVMs; i++)
                cost += vmworkingcost[i];
            for (int i = 0; i < Constants.NoOfTasks; i++) {
                cost += delayPriority[i] * waitcost[mappingPriority[i]];
            }
            System.out.println("The cost for Priority scheduling is " + cost);

            //优先级任务调度最大完工时间



            // Sixth step: Starts the simulation
            CloudSim.startSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

            printCloudletList(newList);

//      mapping_sjf = broker.getMapping();
//      System.out.println("---Mapping---");
//      for (int i = 0; i < Constants.NoOfTasks; i++) {
//	      System.out.print(mapping_sjf[i] + " ");
//      }
//      System.out.println("");

            resultcost[1] = cost;


            Log.printLine("Simulation of Task Scheduler using Priority scheduling is finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }






    private static Datacenter createDatacenter(String name) {
        // Here are the steps needed to create a PowerDatacenter:

        // 1. We need to create a list to store our machine
        List<Host> hostList = new ArrayList<Host>();

        // 2. create hosts, where Every Machine contains one or more PEs or CPUs/Cores

        // ((( Host 1
        // )))--------------------------------------------------------------------------------------------------------
        List<Pe> Host_1_peList = new ArrayList<Pe>();

        // get the mips value of the selected processor
        int Host_1_mips = Processors1.Intel.Core_2_Extreme_X6800.mips;
        // get processor's number of cores
        int Host_1_cores = Processors1.Intel.Core_2_Extreme_X6800.cores;

        // 3. Create PEs and add these into a list.
        for (int i = 0; i < Host_1_cores; i++) {
            // mips/cores => MIPS value is cumulative for all cores so we divide the MIPS value among all
            // of the cores
            Host_1_peList.add(new Pe(i, new PeProvisionerSimple(Host_1_mips / Host_1_cores))); // need to
            // store Pe
            // id and
            // MIPS
            // Rating
        }

        // 4. Create Host with its id and list of PEs and add them to the list of machines
        int host_1_ID = 1;
        int host_1_ram = 2048; // host memory (MB)
        long host_1_storage = 1048576; // host storage in MBs
        int host_1_bw = 10240; // bandwidth in MB/s

        hostList.add(new Host(host_1_ID, new RamProvisionerSimple(host_1_ram),
                new BwProvisionerSimple(host_1_bw), host_1_storage, Host_1_peList,
                new VmSchedulerTimeShared(Host_1_peList)));

        // ((( \Host 1
        // )))--------------------------------------------------------------------------------------------------------

        // ((( Host 2
        // )))--------------------------------------------------------------------------------------------------------

        List<Pe> Host_2_peList = new ArrayList<Pe>();

        // get the mips value of the selected processor
        int Host_2_mips = Processors1.Intel.Core_i7_Extreme_Edition_3960X.mips;
        // get processor's number of cores
        int Host_2_cores = Processors1.Intel.Core_i7_Extreme_Edition_3960X.cores;

        // 3. Create PEs and add these into a list.
        for (int i = 0; i < Host_2_cores; i++) {
            // mips/cores => MIPS value is cumulative for all cores so we divide the MIPS value among all
            // of the cores
            Host_2_peList.add(new Pe(i, new PeProvisionerSimple(Host_2_mips / Host_2_cores))); // need to
            // store Pe
            // id and
            // MIPS
            // Rating
        }

        // 4. Create Host with its id and list of PEs and add them to the list of machines
        int host_2_id = 2;
        int host_2_ram = 2048; // host memory (MB)
        long host_2_storage = 1048576; // host storage in MBs
        int host_2_bw = 10240; // bandwidth in MB/s

        hostList.add(new Host(host_2_id, new RamProvisionerSimple(host_2_ram),
                new BwProvisionerSimple(host_2_bw), host_2_storage, Host_2_peList,
                new VmSchedulerTimeShared(Host_2_peList)));

        // ((( \Host 2
        // )))--------------------------------------------------------------------------------------------------------

        // 5. Create a DatacenterCharacteristics object that stores the
        // properties of a data center: architecture, OS, list of
        // Machines, allocation policy: time- or space-shared, time zone
        // and its price (G$/Pe time unit).
        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this resource
        double costPerBw = 0.0; // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN devices by
        // now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm,
                hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        // 6. Finally, we need to create a PowerDatacenter object.
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList),
                    storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    // Broker policy using PSO
    private static MyDataCenterBroker createBroker() {
        MyDataCenterBroker broker = null;
        try {
            broker = new MyDataCenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    // Broker policy using SJF
    private static SJF_DatacenterBroker createSjfBroker() {
        SJF_DatacenterBroker broker = null;
        try {
            broker = new SJF_DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    // Broker policy using SJF
    private static PriorityDatacenterBroker createPriorityBroker() {
        PriorityDatacenterBroker broker = null;
        try {
            broker = new PriorityDatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    private static List<Vm> createVm(int[] mips, int brokerId) {
        // Fourth step: Create one virtual machine
        List<Vm> vmlist = new ArrayList<Vm>();

        // VM description
        int vmid = 0;
        long size = 10000; // image size (MB)
        int ram = 256; // vm memory (MB)
        long bw = 1000;
        // int pesNumber = 1; //number of cpus
        int pesNumber = 500000;
        String vmm = "Xen"; // VMM name

        // create two VMs
        for (int i = 0; i < Constants.NoOfVMs; i++) {
            Vm vm = new Vm(vmid, brokerId, mips[i], pesNumber, ram, bw, size, vmm,
                    new CloudletSchedulerSpaceShared());
            vmid++;
            // add the VMs to the vmList
            vmlist.add(vm);
        }
        return vmlist;
    }

    private static List<Cloudlet> createCloudLets(int[] tasklength, int[] pesNumber, int[] outputSize,
                                                  int brokerId) {
        List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();
        long fileSize = 300;
        UtilizationModel utilizationModel = new UtilizationModelFull();
        for (int id = 0; id < Constants.NoOfTasks; id++) {
            // pesNumber[id] = 1;
            Cloudlet cloudlet1 = new Cloudlet(id, tasklength[id], pesNumber[id], fileSize, outputSize[id],
                    utilizationModel, utilizationModel, utilizationModel);
            cloudlet1.setUserId(brokerId);
            cloudletList.add(cloudlet1);
        }
        return cloudletList;
    }

    /**
     * Prints the Cloudlet objects
     *
     * @param list list of Cloudlets
     */
    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();

        Cloudlet cloudlet;
        double exetime = 0;


        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID"
                + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent
                        + cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
                        + indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
                        + dft.format(cloudlet.getFinishTime()));


            }
            exetime += cloudlet.getActualCPUTime();
        }

        Log.printLine("+++++++"+dft.format(exetime));
    }



    public static void func_1(int[] tasklength, int[] outputfilesize, int[] mips, double[] execcost,
                              double[] waitcost, int[][] graph, int[] pesNumber) throws Exception {
        /*
         * Depgraph denotes that a task requires output files from which tasks
         * Depgraph表示任务需要输出文件，这些文件来自
         */
        for (int j = 0; j < Constants.NoOfTasks; j++) {
            for (int k = 0; k < Constants.NoOfTasks; k++) {
                depgraph[k][j] = graph[j][k];
            }
        }
        /*
         * Run the PSO to obtain the mapping of cloudlets to VM
         * 运行PSO以获得cloudlets到VM的映射
         */
        KPso1 PSOScheduler = new KPso1(tasklength, outputfilesize, mips, execcost, waitcost, graph);   //得到粒子的速度和位置
        //得到最好的位置
        mapping = PSOScheduler.run();
        // for(int i=0;i<Constants.NoOfVMs;i++)
        // createvmrunscript(i);
        // createscript(mapping,"pso");
//        executiontimematrix = PSOScheduler.getexecutiontimematrix();
//        communicationtimematrix = PSOScheduler.getcommunicationtimematrix();
//        datatransfermatrix = PSOScheduler.getdatatransfermatrix();
//        commcost = PSOScheduler.getcommcost();
//        taskoutputfilematrix = PSOScheduler.gettaskoutputfilematrix();
        Log.printLine("Le Gia Huy (1910202) - Ngo Le Quoc Dung (1910101)");
//正在启动TaskScheduler，其中有一个具有不同虚拟机的数据中心。。。
        Log.printLine(" Starting TaskScheduler having 1 datacenter with different VMs...");

        try {
            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            int num_user = 1; // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);

            // Second step: Create Datacenters
            // Datacenters are the resource providers in CloudSim. We need at list one of them to run a
            // CloudSim simulation
            @SuppressWarnings("unused")
            Datacenter datacenter0 = createDatacenter("Datacenter_0");

            // Third step: Create Broker
            MyDataCenterBroker broker = createBroker();

            int brokerId = broker.getId();


            // //submit vm list to the broker
            vmlist = createVm(mips, brokerId);
            broker.submitVmList(vmlist);

            // Fifth step: Create two Cloudlets
            cloudletList = new ArrayList<Cloudlet>();

            cloudletList = createCloudLets(tasklength, pesNumber, outputfilesize, brokerId);
            // submit cloudlet list to the broker
            broker.submitCloudletList(cloudletList);
            //延迟？？？？
            double delay[] = new double[Constants.NoOfTasks];
            delay[0] = 0;
            for (int i = 0; i < Constants.NoOfTasks; i++) {
                for (int j = i + 1; j < Constants.NoOfTasks; j++) {
                    if (taskoutputfilematrix[i][j] != 0) {
                        delay[j] = Math.max(delay[j], delay[i] + executiontimematrix[i][(int) mapping[i]]
                                + communicationtimematrix[i][j]);
                    }
                }
            }

            broker.submitMapping(mapping);
            broker.submitDelay(delay);

            // Sixth step: Starts the simulation
            CloudSim.startSimulation();

            // Final step: Print results when simulation is over
            List<Cloudlet> newList = broker.getCloudletReceivedList();



            CloudSim.stopSimulation();

            printCloudletList1(newList);

            resultcost[3] = PSOScheduler.printBestFitness();

            Log.printLine("Simulation of Task Scheduler using PSO is finished!");
//      for (int i = 0; i < Constants.NoOfTasks; i++) {
//        System.out.println(mapping[i]);
//
//        System.out.println("----");
//      }
            //执行时间和执行代价
//      for (int i = 0; i < Constants.NoOfTasks; i++) {
//        int vm = (int) mapping[i];  //mapping对应的整数就是分配虚拟机序号，
//        System.out.print("" + executiontimematrix[i][vm] + "*" + execcost[vm] + "\n");
//      }

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }

    }



    public static void func_2(int[] tasklength, int[] outputfilesize, int[] mips, double[] execcost,
                              double[] waitcost, int[][] graph, int[] pesNumber) throws Exception {
        /*
         * Depgraph denotes that a task requires output files from which tasks
         * Depgraph表示任务需要输出文件，这些文件来自
         */
        for (int j = 0; j < Constants.NoOfTasks; j++) {
            for (int k = 0; k < Constants.NoOfTasks; k++) {
                depgraph[k][j] = graph[j][k];
            }
        }
        /*
         * Run the PSO to obtain the mapping of cloudlets to VM
         * 运行PSO以获得cloudlets到VM的映射
         */
        LPSO1 PSOScheduler = new LPSO1(tasklength, outputfilesize, mips, execcost, waitcost, graph);   //得到粒子的速度和位置
        //得到最好的位置
        mapping = PSOScheduler.run();
        // for(int i=0;i<Constants.NoOfVMs;i++)
        // createvmrunscript(i);
        // createscript(mapping,"pso");
//        executiontimematrix = PSOScheduler.getexecutiontimematrix();
//        communicationtimematrix = PSOScheduler.getcommunicationtimematrix();
//        datatransfermatrix = PSOScheduler.getdatatransfermatrix();
//        commcost = PSOScheduler.getcommcost();
//        taskoutputfilematrix = PSOScheduler.gettaskoutputfilematrix();
        Log.printLine("Le Gia Huy (1910202) - Ngo Le Quoc Dung (1910101)");
//正在启动TaskScheduler，其中有一个具有不同虚拟机的数据中心。。。
        Log.printLine("  Starting TaskScheduler having 1 datacenter with different VMs...");

        try {
            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            int num_user = 1; // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);

            // Second step: Create Datacenters
            // Datacenters are the resource providers in CloudSim. We need at list one of them to run a
            // CloudSim simulation
            @SuppressWarnings("unused")
            Datacenter datacenter0 = createDatacenter("Datacenter_0");

            // Third step: Create Broker
            MyDataCenterBroker broker = createBroker();

            int brokerId = broker.getId();


            // //submit vm list to the broker
            vmlist = createVm(mips, brokerId);
            broker.submitVmList(vmlist);

            // Fifth step: Create two Cloudlets
            cloudletList = new ArrayList<Cloudlet>();

            cloudletList = createCloudLets(tasklength, pesNumber, outputfilesize, brokerId);
            // submit cloudlet list to the broker
            broker.submitCloudletList(cloudletList);
            //延迟？？？？
            double delay[] = new double[Constants.NoOfTasks];
            delay[0] = 0;
            for (int i = 0; i < Constants.NoOfTasks; i++) {
                for (int j = i + 1; j < Constants.NoOfTasks; j++) {
                    if (taskoutputfilematrix[i][j] != 0) {
                        delay[j] = Math.max(delay[j], delay[i] + executiontimematrix[i][(int) mapping[i]]
                                + communicationtimematrix[i][j]);
                    }
                }
            }

            broker.submitMapping(mapping);
            broker.submitDelay(delay);

            // Sixth step: Starts the simulation
            CloudSim.startSimulation();

            // Final step: Print results when simulation is over
            List<Cloudlet> newList = broker.getCloudletReceivedList();



            CloudSim.stopSimulation();

            printCloudletList2(newList);

            resultcost[4] = PSOScheduler.printBestFitness();

            Log.printLine("Simulation of Task Scheduler using LPSO is finished!");
//      for (int i = 0; i < Constants.NoOfTasks; i++) {
//        System.out.println(mapping[i]);
//
//        System.out.println("----");
//      }
            //执行时间和执行代价
//      for (int i = 0; i < Constants.NoOfTasks; i++) {
//        int vm = (int) mapping[i];  //mapping对应的整数就是分配虚拟机序号，
//        System.out.print("" + executiontimematrix[i][vm] + "*" + execcost[vm] + "\n");
//      }

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }

    }




    public static void func_3(int[] tasklength, int[] outputfilesize, int[] mips, double[] execcost,
                              double[] waitcost, int[][] graph, int[] pesNumber) throws Exception {
        /*
         * Depgraph denotes that a task requires output files from which tasks
         * Depgraph表示任务需要输出文件，这些文件来自
         */
        for (int j = 0; j < Constants.NoOfTasks; j++) {
            for (int k = 0; k < Constants.NoOfTasks; k++) {
                depgraph[k][j] = graph[j][k];
            }
        }
        /*
         * Run the PSO to obtain the mapping of cloudlets to VM
         * 运行PSO以获得cloudlets到VM的映射
         */
        LKPSO1 PSOScheduler = new LKPSO1(tasklength, outputfilesize, mips, execcost, waitcost, graph);   //得到粒子的速度和位置
        //得到最好的位置
        mapping = PSOScheduler.run();
        // for(int i=0;i<Constants.NoOfVMs;i++)
        // createvmrunscript(i);
        // createscript(mapping,"pso");
//        executiontimematrix = PSOScheduler.getexecutiontimematrix();
//        communicationtimematrix = PSOScheduler.getcommunicationtimematrix();
//        datatransfermatrix = PSOScheduler.getdatatransfermatrix();
//        commcost = PSOScheduler.getcommcost();
//        taskoutputfilematrix = PSOScheduler.gettaskoutputfilematrix();
        Log.printLine("Le Gia Huy (1910202) - Ngo Le Quoc Dung (1910101)");
//正在启动TaskScheduler，其中有一个具有不同虚拟机的数据中心。。。
        Log.printLine(" Starting TaskScheduler having 1 datacenter with different VMs...");

        try {
            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            int num_user = 1; // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);

            // Second step: Create Datacenters
            // Datacenters are the resource providers in CloudSim. We need at list one of them to run a
            // CloudSim simulation
            @SuppressWarnings("unused")
            Datacenter datacenter0 = createDatacenter("Datacenter_0");

            // Third step: Create Broker
            MyDataCenterBroker broker = createBroker();

            int brokerId = broker.getId();


            // //submit vm list to the broker
            vmlist = createVm(mips, brokerId);
            broker.submitVmList(vmlist);

            // Fifth step: Create two Cloudlets
            cloudletList = new ArrayList<Cloudlet>();

            cloudletList = createCloudLets(tasklength, pesNumber, outputfilesize, brokerId);
            // submit cloudlet list to the broker
            broker.submitCloudletList(cloudletList);
            //延迟？？？？
            double delay[] = new double[Constants.NoOfTasks];
            delay[0] = 0;
            for (int i = 0; i < Constants.NoOfTasks; i++) {
                for (int j = i + 1; j < Constants.NoOfTasks; j++) {
                    if (taskoutputfilematrix[i][j] != 0) {
                        delay[j] = Math.max(delay[j], delay[i] + executiontimematrix[i][(int) mapping[i]]
                                + communicationtimematrix[i][j]);
                    }
                }
            }

            broker.submitMapping(mapping);
            broker.submitDelay(delay);

            // Sixth step: Starts the simulation
            CloudSim.startSimulation();

            // Final step: Print results when simulation is over
            List<Cloudlet> newList = broker.getCloudletReceivedList();



            CloudSim.stopSimulation();

            printCloudletList3(newList);

            resultcost[5] = PSOScheduler.printBestFitness();

            Log.printLine("Simulation of Task Scheduler using LKPSO is finished!");
//      for (int i = 0; i < Constants.NoOfTasks; i++) {
//        System.out.println(mapping[i]);
//
//        System.out.println("----");
//      }
            //执行时间和执行代价
//      for (int i = 0; i < Constants.NoOfTasks; i++) {
//        int vm = (int) mapping[i];  //mapping对应的整数就是分配虚拟机序号，
//        System.out.print("" + executiontimematrix[i][vm] + "*" + execcost[vm] + "\n");
//      }

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }

    }

    private static void printCloudletList1(List<Cloudlet> list) {
        int size = list.size();

        Cloudlet cloudlet;
        double exetime1 = 0;


        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID"
                + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent
                        + cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
                        + indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
                        + dft.format(cloudlet.getFinishTime()));


            }
            exetime1 += cloudlet.getActualCPUTime();
        }

        Log.printLine("+++++++"+dft.format(exetime1));

    }

    private static void printCloudletList2(List<Cloudlet> list) {
        int size = list.size();

        Cloudlet cloudlet;
        double exetime2 = 0;


        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID"
                + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent
                        + cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
                        + indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
                        + dft.format(cloudlet.getFinishTime()));


            }
            exetime2 += cloudlet.getActualCPUTime();
        }

        Log.printLine("+++++++"+dft.format(exetime2));
    }
    private static void printCloudletList3(List<Cloudlet> list) {
        int size = list.size();

        Cloudlet cloudlet;
        double exetime3 = 0;


        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID"
                + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent
                        + cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
                        + indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
                        + dft.format(cloudlet.getFinishTime()));


            }
            exetime3 += cloudlet.getActualCPUTime();
        }

        Log.printLine("+++++++"+dft.format(exetime3));
    }
    private static void printCloudletList4(List<Cloudlet> list) {
        int size = list.size();

        Cloudlet cloudlet;
        double exetime4 = 0;


        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID"
                + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent
                        + cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
                        + indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
                        + dft.format(cloudlet.getFinishTime()));


            }
            exetime4 += cloudlet.getActualCPUTime();
        }

        Log.printLine("+++++++"+dft.format(exetime4));
    }

}



