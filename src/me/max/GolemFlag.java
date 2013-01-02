package me.max;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;

public class GolemFlag extends StateFlag{
	 public static GolemFlag flag = new GolemFlag();

	    public GolemFlag() {
	        super("Block_Golems", false);
	    }

	    public static void reflectIntoFlags() {
	        try {
	            Field field = DefaultFlag.class.getDeclaredField("flagsList");

	            Field modifiersField = Field.class.getDeclaredField("modifiers");
	            modifiersField.setAccessible(true);
	            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

	            field.setAccessible(true);

	            List<Flag> elements = elements();

	            Flag<?> list[] = new Flag<?>[elements.size()];
	            for (int i = 0; i < elements.size(); i++)
	                list[i] = elements.get(i);

	            field.set(null, list);

	        } catch (Exception e) {
	           
	            e.printStackTrace();
	        }
	    }
	    private static List elements() {
	        List<Flag> elements = new ArrayList(Arrays.asList(DefaultFlag.getFlags()));
	        elements.add(flag);
	        return elements;
	    }


}
