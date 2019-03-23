/*
 * Copyright 2012 J. Patrick Meyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itemanalysis.psychometrics.scaling;

import com.itemanalysis.psychometrics.statistics.LinearTransformation;

import java.util.Formatter;
import java.util.TreeMap;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class ScoreTable {

    private ScoreBounds sumScoreBounds = null;

    private Double by = 1.0;

    private int precision = 4;

    private TreeMap<Double, Double> table = null;

    private String name = "";

    public ScoreTable(ScoreBounds sumScoreBounds){
        this(sumScoreBounds, 1.0, "Score", 4);
    }

    public ScoreTable(ScoreBounds sumScoreBounds, String name){
        this(sumScoreBounds, 1.0, name, 4);
    }

    public ScoreTable(ScoreBounds sumScoreBounds, int precision){
        this(sumScoreBounds, 1.0, "Score", precision);
    }
    
    public ScoreTable(ScoreBounds sumScoreBounds, String name, int precision){
        this(sumScoreBounds, 1.0, name, precision);
    }

    public ScoreTable(ScoreBounds sumScoreBounds, Double by, String name, int precision){
        this.sumScoreBounds = sumScoreBounds;
        this.by = by;
        this.name = name;
        this.precision = precision;
        table = new TreeMap<Double, Double>();
    }

    public void kelleyScoreTable(KelleyRegressedScore kelley, LinearTransformation linearTransformation,
                                 ScoreBounds scaleScoreBounds, boolean rescale){
        double x = sumScoreBounds.getMinPossibleScore();
        double max = sumScoreBounds.getMaxPossibleScore();
        double k = 0.0;
        while(x<=max){
            k = scaleScoreBounds.checkConstraints(kelley.value(x, linearTransformation));
            table.put(x, k);
            x+=by;
        }
    }

    public void normalizedScoreTable(NormalizedScore normScore, ScoreBounds scaleScoreBounds, boolean rescale){
        Double min = sumScoreBounds.getMinPossibleScore();
        Double x = min;
        Double max = sumScoreBounds.getMaxPossibleScore();
        Double n = 0.0;

        x = min;

        while(x<=max){
            n = scaleScoreBounds.checkConstraints(normScore.getNormalizedScoreAt(x));
            table.put(x, n);
            x+=by;
        }

    }

    public TreeMap<Double, Double> getTable(){
        return table;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        String f1="%10." + precision + "f";
		String f2="%10s";
//		String f3="%10.4f";

        //table header
        f.format("%28s","SCORE TABLE");
        f.format("%n");
        f.format("%45s","=============================================");
        f.format("%n");
        f.format(f2,"Original");
        f.format("%5s", "");
        f.format(f2,"Kelley");
        f.format("%n");

        f.format(f2,"Value");
        f.format("%5s", "");
        f.format(f2,"Score");
        f.format("%n");
        f.format("%45s","---------------------------------------------");
        f.format("%n");

        for(Double d : table.keySet()){
            f.format(f1,d);
            f.format("%5s", "");
            f.format(f1,table.get(d));
            f.format("%5s", "");
            f.format("%n");
        }

        f.format("%45s","=============================================");
        f.format("%n");
        return f.toString();

    }

}
