package com.example.big.troublesome.corp.robot.maker2k;

import java.util.concurrent.atomic.AtomicLong;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RobotService {
    
    private final RobotStorage robots;
    private final AtomicLong productionThreshold;
    
    RobotService() {
        robots = new RobotStorage();
        productionThreshold = new AtomicLong(100);
    }
    
    long getProductionThreshold() {
        return productionThreshold.get();
    }
    
    void setProductionThreshold(long threshold) {
        productionThreshold.getAndSet(threshold);
    }
    
    void waitProductionThreshold() throws InterruptedException {
        Thread.sleep(productionThreshold.longValue());
    }
    
    void addRobot(Robot robot) {
        robots.add(robot);
    }
    
    Robot takeRobot() {
        return robots.take();
    }
    
    int numRobots() {
        return robots.size();
    }

}
