package test1;

import test.Constants;

import java.io.FileReader;
import java.util.*;

public class Simulation1 {

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println(
                "Enter the Number of times you want to compare results of PSO based vs Priority vs SJF");
        int n = sc.nextInt();
        double arrres[][] = new double[n][6];
        FileReader1 file = new FileReader1("G:\\pso-任务调度\\AIData.xlsx");
        for (int i = 0; i < n; i++) {
            /*
             * For Static Case of Fixed 10 tasks on 8 vms with fixed specifications
             * 对于固定规格的8个虚拟机上固定10个任务的静态情况
             */
            file.readFile();
            // int tasklength[]= {3000,2000,1000,5000,4000,3500,2500,1500,6000,1300};
            int[] tasklength = new int[Constants.NoOfTasks];
            tasklength = file.getRunTime();

            //输出任务大小前20个
//			for (int j = 0; j < tasklength.length; j++) {11
//				System.out.print(tasklength[j] + " ");
//			}

//			System.out.println(tasklength.length);//  任务的数量
            int[] pesNumber = new int[Constants.NoOfTasks];
            pesNumber = file.getPesNumber();//分配的处理器数量
            // int outputfilesize[] = {300,400,100,500,350,700,400,800,1000,550};
            Random rand = new Random();
            int[] outputfilesize = new int[Constants.NoOfTasks];
            for (int j = 0; j < Constants.NoOfTasks; j++) {
                outputfilesize[j] = rand.nextInt((1000 - 100) + 1) + 100;
            }
            //虚拟机处理能力
            int mips[] = {50, 80, 20, 30, 25, 60, 40, 30};
            // int mips[]= {8000,5000,1000,1000,500,1500,2000,6000};
            double execcost[] = {6, 10, 2, 0.5, 0.5, 4.5, 2, 7};
            double waitcost[] = {6, 10, 2, 0.5, 0.5, 4.5, 2, 7};

            //继续工作
            int[] precedingJob = file.getPrecedingJob();
            int[][] graph = new int[Constants.NoOfTasks][Constants.NoOfTasks];
            //初始化一个矩阵graph20x20
            for (int j = 0; j < Constants.NoOfTasks; j++)
                for (int k = 0; k < Constants.NoOfTasks; k++)
                    graph[j][k] = 0;
            for (int j = 0; j < Constants.NoOfTasks; j++) {
                if (precedingJob[j] != -1)
                    graph[precedingJob[j] - 1][j] = 1;
            }

            TaskScheduler1 obj = new TaskScheduler1();
            // double ans[]=obj.func(tasklength,outputfilesize,mips,execcost,graph);
            double ans[] = obj.func(tasklength, outputfilesize, mips, execcost, waitcost, graph,
                    pesNumber);
            arrres[i][0] = ans[0];
            arrres[i][1] = ans[1];
            arrres[i][2] = ans[2];
            arrres[i][3] = ans[3];
            arrres[i][4] = ans[4];
            arrres[i][5] = ans[5];
            System.out.println("Cost of PSO Based Scheduling:" + ans[0]
                    + "\nCost of Priority Scheduling:" + ans[1]
                    + "\nCost of SJF Scheduling:" + ans[2]
                    + "\nCost of KPSO Based Scheduling:" + ans[3]
                    + "\nCost of LPSO Based Scheduling:" + ans[4]
                    + "\nCost of LKPSO Based Scheduling:" + ans[5]);
        }
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("PSO" + "\t\t\t\t\t"  + "SJF"+ "\t\t\t\t\t" + "KPSO"+ "\t\t\t\t\t" + "LPSO"+ "\t\t\t\t\t" + "LKPSO");
        for (int i = 0; i < n; i++) {
            System.out.println(arrres[i][0] + "\t\t" + arrres[i][1] + "\t\t" + arrres[i][2]+ "\t\t" + arrres[i][3]+ "\t\t" + arrres[i][4]+ "\t\t" + arrres[i][5]);
        }

    }


}
