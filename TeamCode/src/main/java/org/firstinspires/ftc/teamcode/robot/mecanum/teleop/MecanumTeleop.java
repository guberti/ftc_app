package org.firstinspires.ftc.teamcode.robot.mecanum.teleop;

import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.teamcode.autonomous.controllers.MecanumPurePursuitController;
import org.firstinspires.ftc.teamcode.autonomous.controllers.PurePursuitPath;
import org.firstinspires.ftc.teamcode.common.SimulatableMecanumOpMode;
import org.firstinspires.ftc.teamcode.common.math.Point;
import org.firstinspires.ftc.teamcode.common.math.Pose;
import org.firstinspires.ftc.teamcode.robot.mecanum.MecanumHardware;
import org.firstinspires.ftc.teamcode.robot.mecanum.MecanumPowers;
import org.firstinspires.ftc.teamcode.robot.mecanum.MecanumUtil;
import org.openftc.revextensions2.RevBulkData;

public abstract class MecanumTeleop extends SimulatableMecanumOpMode {
    MecanumHardware robot;
    PurePursuitPath followPath;

    boolean dpadUpPrev, dpadDownPrev;
    int intakePower;

    // Adjustable properties
    public abstract boolean fieldCentric();

    @Override
    public void init() {
        this.robot = this.getRobot();
        robot.initBNO055IMU(hardwareMap);
        followPath = new PurePursuitPath(robot,
                new Point(0, 0),
                new Point(50, 50),
                new Point(-50, 50),
                new Point(-50, -50),
                new Point(50, -50),
                new Point(0, 0)
        );
    }

    @Override
    public void start() {
        robot.initBulkReadTelemetry();
        dpadUpPrev = gamepad1.dpad_up;
        dpadDownPrev = gamepad1.dpad_down;
        intakePower = 0;
    }

    @Override
    public void loop() {
        RevBulkData data = robot.performBulkRead();

        MecanumPowers ppPowers = MecanumPurePursuitController.goToPosition(
                robot.pose(), new Pose(0, 0, 0), 1.0, 1.0);
        if (gamepad1.left_stick_button) {
            robot.setPowers(ppPowers);
        } else if (gamepad1.right_stick_button) {
            followPath.update();
        } else {
            double slowScale = ((1 - gamepad1.left_trigger) * 0.7 + 0.3);
            double leftX = MecanumUtil.deadZone(gamepad1.left_stick_x, 0.05) * slowScale;
            double leftY = MecanumUtil.deadZone(gamepad1.left_stick_y, 0.05) * slowScale;
            double angle = -Math.atan2(leftY, leftX) + Math.PI / 2;
            if (fieldCentric()) {
                angle -= robot.pose().heading;
            }

            double driveScale = Math.sqrt(Math.pow(leftX, 2) + Math.pow(leftY, 2));
            driveScale = Range.clip(driveScale, 0, 1);

            // Exponentiate our turn
            double turn = Math.copySign(
                    Math.pow(MecanumUtil.deadZone(gamepad1.right_stick_x, 0.05), 2),
                    gamepad1.right_stick_x) * slowScale;

            MecanumPowers powers = MecanumUtil.powersFromAngle(angle, driveScale, turn);
            robot.setPowers(powers);
        }

        if (gamepad1.dpad_up && !dpadUpPrev) {
            dpadUpPrev = true;
            if (intakePower == 1) {
                intakePower = 0;
            } else {
                intakePower = 1;
            }
            robot.setIntakePower(intakePower);
        } else if (!gamepad1.dpad_up) {
            dpadUpPrev = false;
        }

        if (gamepad1.dpad_down && !dpadDownPrev) {
            dpadDownPrev = true;
            if (intakePower == -1) {
                intakePower = 0;
            } else {
                intakePower = -1;
            }
            robot.setIntakePower(intakePower);
        } else if (!gamepad1.dpad_up) {
            dpadDownPrev = false;
        }
    }
}