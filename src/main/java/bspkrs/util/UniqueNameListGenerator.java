package bspkrs.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class UniqueNameListGenerator {
   private static UniqueNameListGenerator instance;

   public static UniqueNameListGenerator instance() {
      if(instance == null) {
         instance = new UniqueNameListGenerator();
      }

      return instance;
   }

   public void run() {
      File listFile = new File(new File(CommonUtils.getConfigDir()), "UniqueNames.txt");

      try {
         ArrayList<String> blockList = new ArrayList();
         ArrayList<String> itemList = new ArrayList();

         for(Object obj : Block.blockRegistry.getKeys()) {
            blockList.add((String)obj);
         }

         for(Object obj : Item.itemRegistry.getKeys()) {
            itemList.add((String)obj);
         }

         Collections.sort(blockList);
         Collections.sort(itemList);
         if(listFile.exists()) {
            listFile.delete();
         }

         listFile.createNewFile();
         PrintWriter out = new PrintWriter(new FileWriter(listFile));
         out.println("# generated by bspkrsCore " + (new SimpleDateFormat("yyyyMMdd HH:mm:ss")).format(new Date()));
         out.println();
         out.println("**********************************************");
         out.println("*  ####   #       ###    ###   #   #   ####  *");
         out.println("*  #   #  #      #   #  #   #  #  #   #      *");
         out.println("*  ####   #      #   #  #      ###     ###   *");
         out.println("*  #   #  #      #   #  #      #  #       #  *");
         out.println("*  ####   #####   ###    ####  #   #  ####   *");
         out.println("**********************************************");
         out.println();
         out.println();

         for(String s : blockList) {
            out.println(s);
         }

         out.println();
         out.println();
         out.println("***************************************");
         out.println("*  #####  #####  #####  #   #   ####  *");
         out.println("*    #      #    #      ## ##  #      *");
         out.println("*    #      #    ###    # # #   ###   *");
         out.println("*    #      #    #      #   #      #  *");
         out.println("*  #####    #    #####  #   #  ####   *");
         out.println("***************************************");
         out.println();
         out.println();

         for(String s : itemList) {
            out.println(s);
         }

         out.println();
         out.println();
         out.close();
      } catch (Exception var7) {
         var7.printStackTrace();
      }

   }
}
