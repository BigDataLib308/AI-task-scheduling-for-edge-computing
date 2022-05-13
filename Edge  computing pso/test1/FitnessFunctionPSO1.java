package test1;

/**
 *
 * @author Divyanshu Mishra
 */

import net.sourceforge.jswarm_pso.FitnessFunction;
import test.Constants;

public class      FitnessFunctionPSO1 extends FitnessFunction {


	/*
	 * datatransfermatrix: This matrix denotes the speed of data transfer from one cloudlet to
	 * another taskoutputfilematrix: This matrix denotes the amount of data that is transferred from
	 * one cloudlet to another communicationtimematrix: This matrix represents the time of
	 * communication of outpuut file from one task to another executiontimematrix: This matrix
	 * represents the time of execution required to run a cloudlet on a vm waittime: This array
	 * represents the waittime of each cloudlet graph: This represents the dependency among tasks
	 * outputfilesize: This matrix denotes the size of output files produced by each task mips: This
	 * is the mips rating(Million Instructions per second which are processed) of each VM that is
	 * being called execcost: This is the cost of execution on different VM per unit time. waitcost:
	 * This is the cost of waitcost on different VM per unit time. tasklength: This denotes the task
	 * length of every cloudlet to be executed commcost: This is the cost of communication of result
	 * from one task to another per unit time.
	 * datatransfermatrix：这个矩阵表示从一个cloudlet到另一个cloudlet的数据传输速度
       taskoutputfilematrix：该矩阵表示从一个cloudlet传输到另一个cloudlet的数据量
       * communicationtimematrix：该矩阵表示输出文件从一个任务到另一个任务的通信时间
executiontimematrix：该矩阵表示在vm上运行cloudlet所需的执行时间
waittime：这个数组表示每个cloudlet的等待时间
图表：这表示任务之间的依赖关系
outputfilesize：这个矩阵表示每个任务生成的输出文件的大小
mips：这是被调用的每个VM的mips等级（每秒处理的指令数为百万）
execcost：这是每单位时间在不同VM上执行的成本。
waitcost：这是单位时间内不同VM上的waitcost成本。
tasklength：这表示要执行的每个cloudlet的任务长度commcost：这是单位时间内从一个任务到另一个任务的结果的通信成本

	 */
	private static double[][] executiontimematrix, communicationtimematrix, taskoutputfilematrix,
			datatransfermatrix;
	private static double[] waittime;

	public int graph[][];
	int outputfilesize[];
	int mips[];
	double execcost[];
	double waitcost[];
	int tasklength[];

	double commcost[][] = new double[Constants.NoOfTasks][Constants.NoOfTasks];
    //适应度函数
	FitnessFunctionPSO1(double execcost[], double waitcost[], int mips[], int outputfilesize[],
                        int tasklength[], int graph[][]) {
		/*
		 * Minimization problem so false
		 */
		super(false);
		this.tasklength = tasklength;
		this.execcost = execcost;
		this.mips = mips;
		this.outputfilesize = outputfilesize;
		this.graph = graph;
		this.waitcost = waitcost;
		initializeMatrices();
	}

	private void initializeMatrices() {
		System.out.println("Initializing execution time and communication time matrix");
		executiontimematrix = new double[Constants.NoOfTasks][Constants.NoOfVMs];
		communicationtimematrix = new double[Constants.NoOfTasks][Constants.NoOfTasks];
		taskoutputfilematrix = new double[Constants.NoOfTasks][Constants.NoOfTasks];
		datatransfermatrix = new double[Constants.NoOfTasks][Constants.NoOfTasks];
		waittime = new double[Constants.NoOfTasks];
		/*
		 * Calculation of Execution Time of each task i on vm j
		 * 计算vm j上每个任务i的执行时间
		 */
		for (int i = 0; i < Constants.NoOfTasks; i++) {
			for (int j = 0; j < Constants.NoOfVMs; j++) {
				executiontimematrix[i][j] = tasklength[i] / mips[j];
			}
		}

		//任务传输的数据量
		for (int i = 0; i < Constants.NoOfTasks; i++) {
			for (int j = i; j < Constants.NoOfTasks; j++) {
				if (i == j)
					taskoutputfilematrix[i][j] = 0;
				else
					taskoutputfilematrix[i][j] = outputfilesize[i] * graph[i][j];
			}
		}
		/*
		 * Here the Data Transfer Speed between two cloudlets is considered to be constant
		 * 在这里，两个Cloudlet之间的数据传输速度被认为是恒定的     数据传输速度矩阵
		 */
		for (int i = 0; i < Constants.NoOfTasks; i++) {
			for (int j = i; j < Constants.NoOfTasks; j++) {
				if (i == j)
					datatransfermatrix[i][j] = 0;
				else {
					datatransfermatrix[i][j] = 80;
					datatransfermatrix[j][i] = datatransfermatrix[i][j];
				}
			}
		}
		/*
		 * Calculation of Communication Time from one task to another
		 * 计算通信时间
		 */
		for (int i = 0; i < Constants.NoOfTasks; i++) {
			for (int j = i; j < Constants.NoOfTasks; j++) {
				if (i == j)
					communicationtimematrix[i][j] = 0;
				else {
					communicationtimematrix[i][j] =
							taskoutputfilematrix[i][j] / datatransfermatrix[i][j];
				}
			}
		}
		for (int i = 0; i < Constants.NoOfTasks; i++) {
			for (int j = i; j < Constants.NoOfTasks; j++) {
				if (i == j)
					commcost[i][j] = 0;
				else {
					commcost[i][j] = 3;
					commcost[j][i] = commcost[i][j];
				}
			}
		}
//		printmatrices();

	}

	//LLQ最大完工时间
	public double calcMakespan(double[] position){
		double makespan = 0.0;
		double[] vmworkingtime = new double[Constants.NoOfVMs];
		for (int i = 0; i<Constants.NoOfTasks; i++){
			int vm = (int) position[i];

			if(vmworkingtime[vm] != 0) --vmworkingtime[vm];
			vmworkingtime[vm]+=executiontimematrix[i][vm]
					+ communicationtimematrix[i][vm];
			makespan=Math.max(makespan,vmworkingtime[vm]);


		}
		return makespan;
	}

		public double calculatecost(double[] position) {
		double cost = 0.0;
		double[] vmworkingcost = new double[Constants.NoOfVMs];
		/*
		 * Calculation of waiting time for each task
		 * 计算每个任务的等待时间
		 */
		for (int i = 0; i < Constants.NoOfTasks; i++) {
			for (int j = i + 1; j < Constants.NoOfTasks; j++) {
				if (taskoutputfilematrix[i][j] != 0) {
					waittime[j] = Math.max(waittime[j],
							waittime[i] + executiontimematrix[i][(int) position[i]]
									+ communicationtimematrix[i][j]);
				}

			}


		}


		//虚拟机工作成本
		for (int i = 0; i < Constants.NoOfTasks; i++) {
			int vm = (int) position[i];
			vmworkingcost[vm] += (executiontimematrix[i][vm]) * execcost[vm];
		}
		for (int i = 0; i < Constants.NoOfTasks; i++) {
			int vm = (int) position[i];
			for (int j = i + 1; j < Constants.NoOfTasks; j++) {
				vmworkingcost[vm] += (communicationtimematrix[i][j]) * commcost[i][j];
			}
		}
		for (int i = 0; i < Constants.NoOfVMs; i++)
			cost += vmworkingcost[i];
		for (int i = 0; i < Constants.NoOfTasks; i++) {
			cost += waittime[i] * waitcost[(int) position[i]];
		}
		return cost;
	}

	public double evaluate(double[] position) {
		return calculatecost(position);
	}
    //打印矩阵
	public void printmatrices() {
		System.out.println("Execution Time Matrix");
		for (int i = 0; i < Constants.NoOfTasks; i++) {
			for (int j = 0; j < Constants.NoOfVMs; j++) {
				System.out.print(executiontimematrix[i][j] + "\t");
			}
			System.out.println();
		}
		System.out.println("taskoutputfile Matrix");
		for (int i = 0; i < Constants.NoOfTasks; i++) {
			for (int j = 0; j < Constants.NoOfTasks; j++) {
				System.out.print(taskoutputfilematrix[i][j] + "\t");
			}
			System.out.println();
		}
		System.out.println("datatransfer Matrix");
		for (int i = 0; i < Constants.NoOfTasks; i++) {
			for (int j = 0; j < Constants.NoOfTasks; j++) {
				System.out.print(datatransfermatrix[i][j] + "\t");
			}
			System.out.println();
		}

		System.out.println("Communication Time Matrix");
		for (int i = 0; i < Constants.NoOfTasks; i++) {
			for (int j = 0; j < Constants.NoOfTasks; j++) {
				System.out.print(communicationtimematrix[i][j] + "\t");
			}
			System.out.println();
		}
		System.out.println("Communication cost Matrix");
		for (int i = 0; i < Constants.NoOfTasks; i++) {
			for (int j = 0; j < Constants.NoOfTasks; j++) {
				System.out.print(commcost[i][j] + "\t");
			}
			System.out.println();
		}
	}

	public double[][] getexecutiontimematrix() {
		return executiontimematrix;
	}

	public double[][] getcommunicationtimematrix() {
		return communicationtimematrix;
	}

	public double[][] getdatatransfermatrix() {
		return datatransfermatrix;
	}

	public double[][] getcommcost() {
		return commcost;
	}

	public double[][] gettaskoutputfilematrix() {
		return taskoutputfilematrix;
	}

}

