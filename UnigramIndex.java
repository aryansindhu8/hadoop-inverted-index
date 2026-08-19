import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class UnigramIndex {
   public static class TokenizerMapper extends Mapper<Object, Text, Text, Text>
   {
      // private final static IntWritable one = new IntWritable(1);
      private Text word = new Text();
      
      public void map(Object key, Text value, Context context) throws IOException, InterruptedException 
      {
         String [] shard = value.toString().split("\t");
         if(shard.length<2){
            return;
         }
         String FILE_ID=shard[0].trim();
         String Dict=shard[1].toLowerCase().replaceAll("[^a-z\\s]"," ");

         
         StringTokenizer itr = new StringTokenizer(Dict);
         while (itr.hasMoreTokens()) 
         {
            word.set(itr.nextToken());
            context.write(word, new Text(FILE_ID));
         }
      }
   }
   
   public static class IntSumReducer extends Reducer<Text,Text,Text,Text> 
   {
      public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException 
      {
         HashMap<String,Integer> hm = new HashMap<>();
         
         for (Text val : values) 
         {
            String FILE_ID=val.toString();
            hm.put(FILE_ID,hm.getOrDefault(FILE_ID,0)+1);
         }
         StringBuilder cs = new StringBuilder();
         for(Map.Entry<String,Integer> e:hm.entrySet()){

            cs.append(e.getKey()).append(":").append(e.getValue()).append(", ");
         }
         if(cs.length()>2){
            cs.setLength(cs.length()-2);
         }
         context.write(key, new Text(cs.toString()));
      }
   }
   
   public static void main(String[] args) throws Exception 
   {
      Configuration conf = new Configuration();
      Job job = Job.getInstance(conf, "Part 1: Unigram Count");
		
      job.setJarByClass(UnigramIndex.class);
      job.setMapperClass(TokenizerMapper.class);
      // job.setCombinerClass(IntSumReducer.class);
      job.setReducerClass(IntSumReducer.class);
		
      job.setOutputKeyClass(Text.class);
      job.setOutputValueClass(Text.class);
		
      FileInputFormat.addInputPath(job, new Path(args[0]));
      FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
      System.exit(job.waitForCompletion(true) ? 0 : 1);
   }
}// UnigramIndex

