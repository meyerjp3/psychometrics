package com.itemanalysis.psychometrics.irt;

import com.itemanalysis.psychometrics.WrightMap;
import com.itemanalysis.psychometrics.data.VariableLabel;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.model.IrmPCM;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import org.junit.Test;

public class WrightMapTest {

    @Test
    public void itemMapTest1(){

        double bparam[] = {-0.175319278, -0.313478602, 1.080257034, 0.365667886, -0.37653713, -0.58058991};
        double[][] threshold = {
                {-1.722652915, 0.377301907, -0.73103797, 2.076388978},
                {-1.11401192, -0.159459638, -0.276526919, 1.549998476},
                {-1.451083414, -0.264893332, -0.20019666, 1.916173405},
                {-1.170372031, -0.03438343, -0.225235042, 1.429990503},
                {-1.989285392, 0.193961758, 0.153285593, 1.64203804},
                {-1.709745669, 0.422188581, -0.481972982, 1.769530071}
        };

        ItemResponseModel[] irm = new ItemResponseModel[6];

        for(int i=0;i<6;i++){
            irm[i] = new IrmPCM(bparam[i], threshold[i], 1.0);
            irm[i].setName(new VariableName("Item"+(i+1)));
        }

        String[] respLabels = {"Strongly Disagree", "Disagree", "Neither AG nor DA", "Agree", "Strongly Agree"};

        irm[0].setLabel(new VariableLabel("Assessments required me to demonstrate mastery"));
        irm[1].setLabel(new VariableLabel("Assessments were relevant to course objectives"));
        irm[2].setLabel(new VariableLabel("Instructor provided feedback throughout the semester"));
        irm[3].setLabel(new VariableLabel("Instructor graded assignments in a timely manner"));
        irm[4].setLabel(new VariableLabel("Provided opportunities to apply skills to real-world problems"));
        irm[5].setLabel(new VariableLabel("Assessments challenged me to think critically"));

        WrightMap itMap = new WrightMap(irm, true);
        itMap.setResponseLabels(respLabels);
        itMap.createMap(0.65, -10, 10, 250);

        System.out.println(itMap.toString());

    }


}