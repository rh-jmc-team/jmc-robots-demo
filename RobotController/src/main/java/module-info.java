module RobotController {
    //exports com.example.big.troublesome.corp.robot.controller;

    requires RobotCommons;
    requires org.openjdk.jmc.common;
    requires org.openjdk.jmc.flightrecorder;
    requires jdk.jcmd;
    requires jdk.attach;
    requires java.management;
    requires jdk.management.jfr;
}
