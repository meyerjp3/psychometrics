package com.itemanalysis.psychometrics.classicalitemanalysis;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StreamingItemAnalysisTest extends Application {

    private TableView<ItemResults> table = null;
    private ObservableList data = FXCollections.observableArrayList();
    private ArrayList<TestEventListener> testEventListeners = new ArrayList<TestEventListener>();


    public void start(Stage primaryStage){
        primaryStage.setTitle("Streaming CIA");

        table = new TableView<ItemResults>();
        table.setItems(data);
        table.setPrefWidth(1024);
        table.setPrefWidth(800);

        TableColumn col = null;
        col = new TableColumn("Item Name");
        col.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        table.getColumns().add(col);

        col = new TableColumn("Difficulty");
        col.setCellValueFactory(new PropertyValueFactory<>("itemDifficulty"));
        col.setCellFactory(new DecimalColumnFactory<>(new DecimalFormat("0.0000")));
        table.getColumns().add(col);

        col = new TableColumn("Discrimination");
        col.setCellValueFactory(new PropertyValueFactory<>("itemTotalCorrelation"));
        col.setCellFactory(new DecimalColumnFactory<>(new DecimalFormat("0.0000")));
        table.getColumns().add(col);

        col = new TableColumn("Prop0");
        col.setCellValueFactory(new PropertyValueFactory<>("d1Prop"));
        col.setCellFactory(new DecimalColumnFactory<>(new DecimalFormat("0.0000")));
        table.getColumns().add(col);

        col = new TableColumn("PBis0");
        col.setCellValueFactory(new PropertyValueFactory<>("d1Corr"));
        col.setCellFactory(new DecimalColumnFactory<>(new DecimalFormat("0.0000")));
        table.getColumns().add(col);

        col = new TableColumn("Prop1");
        col.setCellValueFactory(new PropertyValueFactory<>("d2Prop"));
        col.setCellFactory(new DecimalColumnFactory<>(new DecimalFormat("0.0000")));
        table.getColumns().add(col);

        col = new TableColumn("PBis1");
        col.setCellValueFactory(new PropertyValueFactory<>("d2Corr"));
        col.setCellFactory(new DecimalColumnFactory<>(new DecimalFormat("0.0000")));
        table.getColumns().add(col);

        col = new TableColumn("Prop2");
        col.setCellValueFactory(new PropertyValueFactory<>("d3Prop"));
        col.setCellFactory(new DecimalColumnFactory<>(new DecimalFormat("0.0000")));
        table.getColumns().add(col);

        col = new TableColumn("PBis2");
        col.setCellValueFactory(new PropertyValueFactory<>("d3Corr"));
        col.setCellFactory(new DecimalColumnFactory<>(new DecimalFormat("0.0000")));
        table.getColumns().add(col);

        col = new TableColumn("Prop3");
        col.setCellValueFactory(new PropertyValueFactory<>("d4Prop"));
        col.setCellFactory(new DecimalColumnFactory<>(new DecimalFormat("0.0000")));
        table.getColumns().add(col);

        col = new TableColumn("PBis3");
        col.setCellValueFactory(new PropertyValueFactory<>("d4Corr"));
        col.setCellFactory(new DecimalColumnFactory<>(new DecimalFormat("0.0000")));
        table.getColumns().add(col);

        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        vbox.getChildren().add(table);

        Scene scene = new Scene(vbox, 1024, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        initialize();
        loadData();

    }

    private void initialize(){
        ItemResults itemResults = new ItemResults();
        testEventListeners.add(itemResults);

        ItemStatisticsModel itemStatisticsModel = null;

        for(int i=0;i<20;i++){
            itemStatisticsModel = new ItemStatisticsModel("item"+(i+1));
            itemResults.addItem(itemStatisticsModel);
            data.add(itemStatisticsModel);
        }
    }

    private void loadData(){

        ExecutorService es = Executors.newSingleThreadScheduledExecutor();

            es.execute(new Runnable() {

                @Override
                public void run() {

                    try{
                        //This example uses data with 20 items
                        File f = FileUtils.toFile(this.getClass().getResource("/testdata/jalabert-jan15.csv"));
                        BufferedReader br = new BufferedReader(new FileReader(f));

                        //skip first line because it has variable names in this example
                        br.readLine();

                        String line = "";

                        int nItems = 20;
                        int rowCount = 0;
                        double rawScore = 0;
                        double responseScore = 0;
                        String[] s = null;
                        double[] responseVector = null;
                        String[] itemName = new String[nItems];

                        //loop over examinees
                        while((line=br.readLine())!=null){
                            rawScore = 0;
                            s = line.split(",");
                            responseVector = new double[s.length];

                            //loop over individual items
                            for(int i=0; i<s.length;i++){
                                if(rowCount==0){
                                    itemName[i] = "item"+(i+1);
                                }
                                responseScore = Double.parseDouble(s[i]);
                                responseVector[i] = responseScore;
                                rawScore += responseScore;
                            }

                            //update test event listeners
                            for(TestEventListener l : testEventListeners){
                                l.incrementItemResponseVector(itemName, s, responseVector, rawScore);
                            }
                            table.refresh();

                            //pause to make updated statistics visible to human. Just here for demo.
//                            if(rowCount>50){
//                                try{
//                                    Thread.sleep(10);
//                                }catch(InterruptedException ex){
//                                    ex.printStackTrace();
//                                }
//                            }


                            rowCount++;

                        }//end while loop over examinees

                        br.close();


                    }catch(IOException ex){
                        ex.printStackTrace();
                    }


                }
            });


    }

    public static void main(String[] args){
        Application.launch(args);
    }

    public class DecimalColumnFactory<S, T extends Number> implements Callback<TableColumn<S, T>, TableCell<S, T>> {

        private DecimalFormat format;

        public DecimalColumnFactory(DecimalFormat format) {
            super();
            this.format = format;
        }

        @Override
        public TableCell<S, T> call(TableColumn<S, T> param) {
            TableCell cell = new TableCell<S, T>() {

                @Override
                protected void updateItem(T item, boolean empty) {
                    if (!empty && item != null) {
                        setText(format.format(item.doubleValue()));
                    } else {
                        setText("");
                    }
                }
            };

            cell.setAlignment(Pos.BASELINE_RIGHT);
            return cell;
        }
    }

    public class ItemStatisticsModel{

        int count = 0;
        private SimpleStringProperty itemName;
        private SimpleDoubleProperty itemDifficulty;
        private SimpleDoubleProperty itemTotalCorrelation;
        private SimpleDoubleProperty d1Prop;
        private SimpleDoubleProperty d1Corr;
        private SimpleDoubleProperty d2Prop;
        private SimpleDoubleProperty d2Corr;
        private SimpleDoubleProperty d3Prop;
        private SimpleDoubleProperty d3Corr;
        private SimpleDoubleProperty d4Prop;
        private SimpleDoubleProperty d4Corr;
        private StreamingItemAnalysis streamingItemAnalysis;

        public ItemStatisticsModel(String itemName){
            this.itemName = new SimpleStringProperty(itemName);
            this.itemDifficulty = new SimpleDoubleProperty();
            this.itemTotalCorrelation = new SimpleDoubleProperty();
            this.d1Prop = new SimpleDoubleProperty();
            this.d1Corr = new SimpleDoubleProperty();
            this.d2Prop = new SimpleDoubleProperty();
            this.d2Corr = new SimpleDoubleProperty();
            this.d3Prop = new SimpleDoubleProperty();
            this.d3Corr = new SimpleDoubleProperty();
            this.d4Prop = new SimpleDoubleProperty();
            this.d4Corr = new SimpleDoubleProperty();
            streamingItemAnalysis = new StreamingItemAnalysis(itemName, true, false, true, DiscriminationType.PEARSON);
        }

        public String getItemName(){
            return itemName.get();
        }

        public double getItemDifficulty(){

            return itemDifficulty.get();
        }

        public double getItemTotalCorrelation(){
            return itemTotalCorrelation.get();
        }

        public double getD1Prop(){
            return d1Prop.get();
        }

        public double getD1Corr(){
            return d1Corr.get();
        }

        public double getD2Prop(){
            return d2Prop.get();
        }

        public double getD2Corr(){
            return d2Corr.get();
        }

        public double getD3Prop(){
            return d3Prop.get();
        }

        public double getD3Corr(){
            return d3Corr.get();
        }

        public double getD4Prop(){
            return d4Prop.get();
        }

        public double getD4Corr(){
            return d4Corr.get();
        }

        /**
         * This method will update the streaming item statistics and set the values in the model.
         *
         * @param itemName an item name
         * @param itemResponse an item response
         * @param itemResponseScore the numeric value of the response
         * @param testScore the test score
         */
        public void incrementItemResponse(String itemName, String itemResponse, double itemResponseScore, double testScore){
            if(!this.itemName.get().equals(itemName)) return;

            streamingItemAnalysis.increment(itemResponse, itemResponseScore, testScore);

            if(count > 50){//starting after 50 because need enough data to avoid NaN, which cause program to terminate when formatting value for table display
                itemDifficulty.set(streamingItemAnalysis.getItemDifficulty());
                itemTotalCorrelation.set(streamingItemAnalysis.getItemTotalCorrelation());

                d1Prop.set(streamingItemAnalysis.getProportionAt("0"));
                d1Corr.set(streamingItemAnalysis.getPointBiserialAt("0"));

                d2Prop.set(streamingItemAnalysis.getProportionAt("1"));
                d2Corr.set(streamingItemAnalysis.getPointBiserialAt("1"));

                d3Prop.set(streamingItemAnalysis.getProportionAt("2"));
                d3Corr.set(streamingItemAnalysis.getPointBiserialAt("2"));

                d4Prop.set(streamingItemAnalysis.getProportionAt("3"));
                d4Corr.set(streamingItemAnalysis.getPointBiserialAt("3"));
            }

            count++;

//            System.out.println(streamingItemAnalysis.toString());
        }

    }

    public class ItemResults implements TestEventListener {

        HashMap<String, ItemStatisticsModel> itemStatisticsMap = new HashMap<String, ItemStatisticsModel>();

        public ItemResults(){

        }

        public void addItem(ItemStatisticsModel itemStatisticsModel){
            ItemStatisticsModel temp = itemStatisticsMap.get(itemStatisticsModel.getItemName());
            if(null==temp){
                itemStatisticsMap.put(itemStatisticsModel.getItemName(), itemStatisticsModel);
            }
        }

        public void incrementItemResponseVector(String[] itemName, String[] itemResponseVector, double[] itemResponseScoreVector, double testScore){
            for(int i=0;i<itemName.length; i++){
                ItemStatisticsModel temp = itemStatisticsMap.get(itemName[i]);
                temp.incrementItemResponse(itemName[i], itemResponseVector[i], itemResponseScoreVector[i], testScore);
            }
        }

        public ItemStatisticsModel getItemStatisticsModel(String itemName){
            return itemStatisticsMap.get(itemName);
        }


    }

    public interface TestEventListener{
        void incrementItemResponseVector(String[] itemName, String[] itemResponseVector, double[] itemResponseScoreVector, double testScore);
    }

}
