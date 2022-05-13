package test1;


import net.sourceforge.jswarm_pso.Particle;
import net.sourceforge.jswarm_pso.ParticleUpdate;
import net.sourceforge.jswarm_pso.Swarm;
import test.Constants;
import test.ParticlePSO;

/*
 * Class Name: PSO Position and Velocity Update Purpose: It is used to update the position and
 * velocity of the given particle
 *
 */
public class KpsoModifiedPSOUpdate extends ParticleUpdate {

    /*
     * Global Parameters: obj : is the object used to store the particle as an object.
     *
     */
    ParticlePSO1 obj;

    KpsoModifiedPSOUpdate(ParticlePSO1 particle) {
        super(particle);
        this.obj = particle;
    }

    /*
     * update : provided by JSwarm and used to update the position and velocity
     *
     */
    public void update(Swarm swarm, Particle particle) {

        double v[] = particle.getVelocity();
        double x[] = particle.getPosition();
        double pbest[] = particle.getBestPosition();
        double gbest[] = swarm.getBestPosition();
        double wmax=0.9;
        double wmin=0.4;
        double c1=2.05;
        double c2=2.05;
        double c=c1+c2;

        /*
         * count : stores the number of iterations of each particle has been updated.
         * 计数：存储每个粒子已更新的迭代次数。it 当前迭代次数
         */
        obj.count = obj.count + 1;
        int it = obj.count;
        /*
         * w : represents inertia weight which has been calculated based on number of iterations k :
         * represents the constriction factor which has been calculated based on number of
         * iterations
         * w：表示根据迭代次数计算的惯性权重k：表示根据迭代次数计算的收缩系数
         */

          double k=2/Math.abs(2+4*c-Math.sqrt(2*Math.pow(c,2)-4*c));   //收缩因子
          double w = wmax-(wmax-wmin)*it/Constants.NoOfIterations;
//		System.out.println(k);

        for (int i = 0; i < Constants.NoOfTasks; i++) {
			/*
			 * There are 4 velocity update functions 1 -> Standard Velocity Update Function 2 ->
			 * Velocity Update using Modified Inertia Weight 3 -> Velocity Update using Constriction
			 * Factor 4 -> Velocity Update using both Inertia Weight and Constriction Factor
			 *1->标准速度更新功能
             2->使用修改的惯性权重更新速度
             3->使用收缩系数更新速度
             4->使用惯性重量和收缩系数更新速度
			 /
			/*
			 * Uncomment the update function that is required to change the update function.
			 */
            // 1.

            // 3.
           v[i]=v[i]+k*(2*Math.random()*(pbest[i]-x[i])+2*Math.random()*(gbest[i]-x[i]));

            particle.setVelocity(v);
            x[i] = (x[i] + v[i]);
            particle.setPosition(x);
        }

    }
}
