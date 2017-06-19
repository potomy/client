package net.minecraft.potion;

import java.util.*;

/**
 * Potion brewing requires a bit of understanding regarding bits (see what I did there? XD) as PotionHelper uses it
 * to determine final brewing products
 *
 * For future ease of reference I will write the significant points down here
 * Bits 0-3 are used to determine which potion will be made
 * Bit 4 determines if it's an awkward potion (brewed using nether wart), always gets unset by effect ingredients
 *     such as ghast tear, golden carrot, etc
 * Bit 5 means the potion is Extended
 * Bit 6 means the potion is Level II (cannot be both Extended and Level II)
 * Bits 7-12 seem to do nothing, although bit 7 gets unset in both the glowstone and redstone potion effects
 * Bit 13 determines if it is drinkable, always gets set by effect ingredients
 * Bit 14 determines if it is splashable (cannot be both drinkable and splashable)
 *
 * +(x) set bit at position 'x' (makes it 1)
 * -(x) unset bit at position 'x' (makes it 0)
 *
 * & functions like the 'AND' condition. The previous bit must be how its listed 'AND' the following bit as well
 * ! functions like the 'NOT' condition. Checks if the following bit is unset.
 *
 * Detailed Example:
 * A water bottle in the brewery is considered to have all the bits unset (bits 0 through 14)
 * Per typical potion creation, the water bottle gets brewed with nether wart, setting bit 4, and creating an
 *     awkward potion. Right now the only bit set is bit 4; no others
 * Lets then continue and make a regeneration potion, adding a ghast tear as the next ingredient
 * In PotionHelper the ghast tear's effect is "+0-1-2-3&4-4+13", which means that when the potion is brewed bit 0 is
 *     set, and bits 1, 2, and 3 are unset. The next part is a bit tricky. What we have remaining is "&4-4+13". What
 *     basically happens is it asks if bit 4 is set. If so, unset it, making sure it is no longer considered an
 *     awkward potion. Then it proceeds as normal and sets bit 13, as by default the first level of potions are
 *     drinkable. Now the set bits are 0 and 13, while the unset bits are 1, 2, and 3
 * Since the regeneration potion's 'potionRequirements' is "0 & !1 & !2 & !3 & 0+6", the game recognizes we have
 *     brewed a basic regeneration potion with duration equal to 2 minutes. The "& 0+6" seems to serve no purpose.
 *     Although it looks as if it would set bit 6 (the Extended flag), it clearly doesn't. Also my effect bit
 *     strings don't include anything similar and yet work fine.
 * If you want your potion to be amplified (a.k.a. Level II) with glowstone you have to add it to PotionHelper's
 *     'potionAmplifiers' ArrayList.
 */
public class PotionHelper
{
    public static final String field_77924_a = null;
    public static final String sugarEffect = "-0+1-2-3&4-4+13";
    public static final String ghastTearEffect = "+0-1-2-3&4-4+13";
    public static final String spiderEyeEffect = "-0-1+2-3&4-4+13";
    public static final String fermentedSpiderEyeEffect = "-0+3-4+13";
    public static final String speckledMelonEffect = "+0-1+2-3&4-4+13";
    public static final String blazePowderEffect = "+0-1-2+3&4-4+13";
    public static final String magmaCreamEffect = "+0+1-2-3&4-4+13";
    public static final String redstoneEffect = "-5+6-7";
    public static final String glowstoneEffect = "+5-6-7";
    public static final String gunpowderEffect = "+14&13-13";
    public static final String goldenCarrotEffect = "-0+1+2-3+13&4-4";
    public static final String field_151423_m = "+0-1+2+3+13&4-4";
    public static final String rabbitFootEffect = "+0+1-2+3&4-4+13";
    public static final String featherEffect = "+0+1+2-3&4-4+13";
    private static final HashMap potionRequirements = new HashMap();

    /** Potion effect amplifier map */
    private static final HashMap potionAmplifiers = new HashMap();
    private static final HashMap field_77925_n = new HashMap();

    /** An array of possible potion prefix names, as translation IDs. */
    private static final String[] potionPrefixes = new String[] {"potion.prefix.mundane", "potion.prefix.uninteresting",
            "potion.prefix.bland", "potion.prefix.clear", "potion.prefix.milky", "potion.prefix.diffuse",
            "potion.prefix.artless", "potion.prefix.thin", "potion.prefix.awkward", "potion.prefix.flat",
            "potion.prefix.bulky", "potion.prefix.bungling", "potion.prefix.buttered", "potion.prefix.smooth",
            "potion.prefix.suave", "potion.prefix.debonair", "potion.prefix.thick", "potion.prefix.elegant",
            "potion.prefix.fancy", "potion.prefix.charming", "potion.prefix.dashing", "potion.prefix.refined",
            "potion.prefix.cordial", "potion.prefix.sparkling", "potion.prefix.potent", "potion.prefix.foul",
            "potion.prefix.odorless", "potion.prefix.rank", "potion.prefix.harsh", "potion.prefix.acrid",
            "potion.prefix.gross", "potion.prefix.stinky"};


    /**
     * Is the bit given set to 1?
     */
    public static boolean checkFlag(int p_77914_0_, int p_77914_1_)
    {
        return (p_77914_0_ & 1 << p_77914_1_) != 0;
    }

    /**
     * Returns 1 if the flag is set, 0 if it is not set.
     */
    private static int isFlagSet(int p_77910_0_, int p_77910_1_)
    {
        return checkFlag(p_77910_0_, p_77910_1_) ? 1 : 0;
    }

    /**
     * Returns 0 if the flag is set, 1 if it is not set.
     */
    private static int isFlagUnset(int p_77916_0_, int p_77916_1_)
    {
        return checkFlag(p_77916_0_, p_77916_1_) ? 0 : 1;
    }

    public static int func_77909_a(int p_77909_0_)
    {
        return func_77908_a(p_77909_0_, 5, 4, 3, 2, 1);
    }

    /**
     * Given a {@link Collection}<{@link PotionEffect}> will return an Integer color.
     */
    public static int calcPotionLiquidColor(Collection p_77911_0_)
    {
        int var1 = 3694022;

        if (p_77911_0_ != null && !p_77911_0_.isEmpty())
        {
            float var2 = 0.0F;
            float var3 = 0.0F;
            float var4 = 0.0F;
            float var5 = 0.0F;
            Iterator var6 = p_77911_0_.iterator();

            while (var6.hasNext())
            {
                PotionEffect var7 = (PotionEffect)var6.next();
                int var8 = Potion.potionTypes[var7.getPotionID()].getLiquidColor();

                for (int var9 = 0; var9 <= var7.getAmplifier(); ++var9)
                {
                    var2 += (float)(var8 >> 16 & 255) / 255.0F;
                    var3 += (float)(var8 >> 8 & 255) / 255.0F;
                    var4 += (float)(var8 >> 0 & 255) / 255.0F;
                    ++var5;
                }
            }

            var2 = var2 / var5 * 255.0F;
            var3 = var3 / var5 * 255.0F;
            var4 = var4 / var5 * 255.0F;
            return (int)var2 << 16 | (int)var3 << 8 | (int)var4;
        }
        else
        {
            return var1;
        }
    }

    public static boolean func_82817_b(Collection p_82817_0_)
    {
        Iterator var1 = p_82817_0_.iterator();
        PotionEffect var2;

        do
        {
            if (!var1.hasNext())
            {
                return true;
            }

            var2 = (PotionEffect)var1.next();
        }
        while (var2.getIsAmbient());

        return false;
    }

    public static int func_77915_a(int p_77915_0_, boolean p_77915_1_)
    {
        if (!p_77915_1_)
        {
            if (field_77925_n.containsKey(Integer.valueOf(p_77915_0_)))
            {
                return ((Integer)field_77925_n.get(Integer.valueOf(p_77915_0_))).intValue();
            }
            else
            {
                int var2 = calcPotionLiquidColor(getPotionEffects(p_77915_0_, false));
                field_77925_n.put(Integer.valueOf(p_77915_0_), Integer.valueOf(var2));
                return var2;
            }
        }
        else
        {
            return calcPotionLiquidColor(getPotionEffects(p_77915_0_, p_77915_1_));
        }
    }

    public static String func_77905_c(int p_77905_0_)
    {
        int var1 = func_77909_a(p_77905_0_);
        return potionPrefixes[var1];
    }

    private static int func_77904_a(boolean p_77904_0_, boolean p_77904_1_, boolean p_77904_2_, int p_77904_3_, int p_77904_4_, int p_77904_5_, int p_77904_6_)
    {
        int var7 = 0;

        if (p_77904_0_)
        {
            var7 = isFlagUnset(p_77904_6_, p_77904_4_);
        }
        else if (p_77904_3_ != -1)
        {
            if (p_77904_3_ == 0 && countSetFlags(p_77904_6_) == p_77904_4_)
            {
                var7 = 1;
            }
            else if (p_77904_3_ == 1 && countSetFlags(p_77904_6_) > p_77904_4_)
            {
                var7 = 1;
            }
            else if (p_77904_3_ == 2 && countSetFlags(p_77904_6_) < p_77904_4_)
            {
                var7 = 1;
            }
        }
        else
        {
            var7 = isFlagSet(p_77904_6_, p_77904_4_);
        }

        if (p_77904_1_)
        {
            var7 *= p_77904_5_;
        }

        if (p_77904_2_)
        {
            var7 *= -1;
        }

        return var7;
    }

    /**
     * Count the number of bits in an integer set to ON.
     */
    private static int countSetFlags(int p_77907_0_)
    {
        int var1;

        for (var1 = 0; p_77907_0_ > 0; ++var1)
        {
            p_77907_0_ &= p_77907_0_ - 1;
        }

        return var1;
    }

    private static int parsePotionEffects(String p_77912_0_, int p_77912_1_, int p_77912_2_, int p_77912_3_)
    {
        if (p_77912_1_ < p_77912_0_.length() && p_77912_2_ >= 0 && p_77912_1_ < p_77912_2_)
        {
            int var4 = p_77912_0_.indexOf(124, p_77912_1_);
            int var5;
            int var17;

            if (var4 >= 0 && var4 < p_77912_2_)
            {
                var5 = parsePotionEffects(p_77912_0_, p_77912_1_, var4 - 1, p_77912_3_);

                if (var5 > 0)
                {
                    return var5;
                }
                else
                {
                    var17 = parsePotionEffects(p_77912_0_, var4 + 1, p_77912_2_, p_77912_3_);
                    return var17 > 0 ? var17 : 0;
                }
            }
            else
            {
                var5 = p_77912_0_.indexOf(38, p_77912_1_);

                if (var5 >= 0 && var5 < p_77912_2_)
                {
                    var17 = parsePotionEffects(p_77912_0_, p_77912_1_, var5 - 1, p_77912_3_);

                    if (var17 <= 0)
                    {
                        return 0;
                    }
                    else
                    {
                        int var18 = parsePotionEffects(p_77912_0_, var5 + 1, p_77912_2_, p_77912_3_);
                        return var18 <= 0 ? 0 : (var17 > var18 ? var17 : var18);
                    }
                }
                else
                {
                    boolean var6 = false;
                    boolean var7 = false;
                    boolean var8 = false;
                    boolean var9 = false;
                    boolean var10 = false;
                    byte var11 = -1;
                    int var12 = 0;
                    int var13 = 0;
                    int var14 = 0;

                    for (int var15 = p_77912_1_; var15 < p_77912_2_; ++var15)
                    {
                        char var16 = p_77912_0_.charAt(var15);

                        if (var16 >= 48 && var16 <= 57)
                        {
                            if (var6)
                            {
                                var13 = var16 - 48;
                                var7 = true;
                            }
                            else
                            {
                                var12 *= 10;
                                var12 += var16 - 48;
                                var8 = true;
                            }
                        }
                        else if (var16 == 42)
                        {
                            var6 = true;
                        }
                        else if (var16 == 33)
                        {
                            if (var8)
                            {
                                var14 += func_77904_a(var9, var7, var10, var11, var12, var13, p_77912_3_);
                                var9 = false;
                                var10 = false;
                                var6 = false;
                                var7 = false;
                                var8 = false;
                                var13 = 0;
                                var12 = 0;
                                var11 = -1;
                            }

                            var9 = true;
                        }
                        else if (var16 == 45)
                        {
                            if (var8)
                            {
                                var14 += func_77904_a(var9, var7, var10, var11, var12, var13, p_77912_3_);
                                var9 = false;
                                var10 = false;
                                var6 = false;
                                var7 = false;
                                var8 = false;
                                var13 = 0;
                                var12 = 0;
                                var11 = -1;
                            }

                            var10 = true;
                        }
                        else if (var16 != 61 && var16 != 60 && var16 != 62)
                        {
                            if (var16 == 43 && var8)
                            {
                                var14 += func_77904_a(var9, var7, var10, var11, var12, var13, p_77912_3_);
                                var9 = false;
                                var10 = false;
                                var6 = false;
                                var7 = false;
                                var8 = false;
                                var13 = 0;
                                var12 = 0;
                                var11 = -1;
                            }
                        }
                        else
                        {
                            if (var8)
                            {
                                var14 += func_77904_a(var9, var7, var10, var11, var12, var13, p_77912_3_);
                                var9 = false;
                                var10 = false;
                                var6 = false;
                                var7 = false;
                                var8 = false;
                                var13 = 0;
                                var12 = 0;
                                var11 = -1;
                            }

                            if (var16 == 61)
                            {
                                var11 = 0;
                            }
                            else if (var16 == 60)
                            {
                                var11 = 2;
                            }
                            else if (var16 == 62)
                            {
                                var11 = 1;
                            }
                        }
                    }

                    if (var8)
                    {
                        var14 += func_77904_a(var9, var7, var10, var11, var12, var13, p_77912_3_);
                    }

                    return var14;
                }
            }
        }
        else
        {
            return 0;
        }
    }

    /**
     * Returns a list of effects for the specified potion damage value.
     */
    public static List getPotionEffects(int p_77917_0_, boolean p_77917_1_)
    {
        ArrayList var2 = null;
        Potion[] var3 = Potion.potionTypes;
        int var4 = var3.length;

        for (int var5 = 0; var5 < var4; ++var5)
        {
            Potion var6 = var3[var5];

            if (var6 != null && (!var6.isUsable() || p_77917_1_))
            {
                String var7 = (String)potionRequirements.get(Integer.valueOf(var6.getId()));

                if (var7 != null)
                {
                    int var8 = parsePotionEffects(var7, 0, var7.length(), p_77917_0_);

                    if (var8 > 0)
                    {
                        int var9 = 0;
                        String var10 = (String)potionAmplifiers.get(Integer.valueOf(var6.getId()));

                        if (var10 != null)
                        {
                            var9 = parsePotionEffects(var10, 0, var10.length(), p_77917_0_);

                            if (var9 < 0)
                            {
                                var9 = 0;
                            }
                        }

                        if (var6.isInstant())
                        {
                            var8 = 1;
                        }
                        else
                        {
                            var8 = 1200 * (var8 * 3 + (var8 - 1) * 2);
                            var8 >>= var9;
                            var8 = (int)Math.round((double)var8 * var6.getEffectiveness());

                            if ((p_77917_0_ & 16384) != 0)
                            {
                                var8 = (int)Math.round((double)var8 * 0.75D + 0.5D);
                            }
                        }

                        if (var2 == null)
                        {
                            var2 = new ArrayList();
                        }

                        PotionEffect var11 = new PotionEffect(var6.getId(), var8, var9);

                        if ((p_77917_0_ & 16384) != 0)
                        {
                            var11.setSplashPotion(true);
                        }

                        var2.add(var11);
                    }
                }
            }
        }

        return var2;
    }

    /**
     * Does bit operations for brewPotionData, given data, the index of the bit being operated upon, whether the bit
     * will be removed, whether the bit will be toggled (NOT), or whether the data field will be set to 0 if the bit is
     * not present.
     */
    private static int brewBitOperations(int p_77906_0_, int p_77906_1_, boolean p_77906_2_, boolean p_77906_3_, boolean p_77906_4_)
    {
        if (p_77906_4_)
        {
            if (!checkFlag(p_77906_0_, p_77906_1_))
            {
                return 0;
            }
        }
        else if (p_77906_2_)
        {
            p_77906_0_ &= ~(1 << p_77906_1_);
        }
        else if (p_77906_3_)
        {
            if ((p_77906_0_ & 1 << p_77906_1_) == 0)
            {
                p_77906_0_ |= 1 << p_77906_1_;
            }
            else
            {
                p_77906_0_ &= ~(1 << p_77906_1_);
            }
        }
        else
        {
            p_77906_0_ |= 1 << p_77906_1_;
        }

        return p_77906_0_;
    }

    /**
     * Generate a data value for a potion, given its previous data value and the encoded string of new effects it will
     * receive
     */
    public static int applyIngredient(int previousData, String encodedString)
    {
        int len = encodedString.length();
        boolean var4 = false;
        boolean var5 = false;
        boolean var6 = false;
        boolean var7 = false;
        int var8 = 0;

        for (int i = 0; i < len; ++i)
        {
            char ch = encodedString.charAt(i);

            if (ch >= 48 && ch <= 57)
            {
                var8 *= 10;
                var8 += ch - 48;
                var4 = true;
            }
            else if (ch == 33)
            {
                if (var4)
                {
                    previousData = brewBitOperations(previousData, var8, var6, var5, var7);
                    var7 = false;
                    var5 = false;
                    var6 = false;
                    var4 = false;
                    var8 = 0;
                }

                var5 = true;
            }
            else if (ch == 45)
            {
                if (var4)
                {
                    previousData = brewBitOperations(previousData, var8, var6, var5, var7);
                    var7 = false;
                    var5 = false;
                    var6 = false;
                    var4 = false;
                    var8 = 0;
                }

                var6 = true;
            }
            else if (ch == 43)
            {
                if (var4)
                {
                    previousData = brewBitOperations(previousData, var8, var6, var5, var7);
                    var7 = false;
                    var5 = false;
                    var6 = false;
                    var4 = false;
                    var8 = 0;
                }
            }
            else if (ch == 38)
            {
                if (var4)
                {
                    previousData = brewBitOperations(previousData, var8, var6, var5, var7);
                    var7 = false;
                    var5 = false;
                    var6 = false;
                    var4 = false;
                    var8 = 0;
                }

                var7 = true;
            }
        }

        if (var4)
        {
            previousData = brewBitOperations(previousData, var8, var6, var5, var7);
        }

        return previousData & 32767;
    }

    public static int func_77908_a(int p_77908_0_, int p_77908_1_, int p_77908_2_, int p_77908_3_, int p_77908_4_, int p_77908_5_)
    {
        return (checkFlag(p_77908_0_, p_77908_1_) ? 16 : 0) | (checkFlag(p_77908_0_, p_77908_2_) ? 8 : 0) | (checkFlag(p_77908_0_, p_77908_3_) ? 4 : 0) | (checkFlag(p_77908_0_, p_77908_4_) ? 2 : 0) | (checkFlag(p_77908_0_, p_77908_5_) ? 1 : 0);
    }

    static
    {
        potionRequirements.put(Potion.regeneration.getId(), "0 & !1 & !2 & !3 & 0+6"); // 1000
        potionRequirements.put(Potion.moveSpeed.getId(), "!0 & 1 & !2 & !3 & 1+6");    // 0100
        potionRequirements.put(Potion.fireResistance.getId(), "0 & 1 & !2 & !3 & 0+6");// 1100
        potionRequirements.put(Potion.heal.getId(), "0 & !1 & 2 & !3");                // 1010
        potionRequirements.put(Potion.poison.getId(), "!0 & !1 & 2 & !3 & 2+6");       // 0010
        potionRequirements.put(Potion.weakness.getId(), "!0 & !1 & !2 & 3 & 3+6");     // 0001
        potionRequirements.put(Potion.harm.getId(), "!0 & !1 & 2 & 3");                // 0011
        potionRequirements.put(Potion.moveSlowdown.getId(), "!0 & 1 & !2 & 3 & 3+6");  // 0101
        potionRequirements.put(Potion.damageBoost.getId(), "0 & !1 & !2 & 3 & 3+6");   // 1001
        potionRequirements.put(Potion.nightVision.getId(), "!0 & 1 & 2 & !3 & 2+6");   // 0110
        potionRequirements.put(Potion.invisibility.getId(), "!0 & 1 & 2 & 3 & 2+6");   // 0111
        potionRequirements.put(Potion.waterBreathing.getId(), "0 & !1 & 2 & 3 & 2+6"); // 1011
        potionRequirements.put(Potion.jump.getId(), "0 & 1 & !2 & 3 & 3+6");           // 1101
        potionRequirements.put(Potion.featherFalling.getId(), "0 & 1 & 2 & !3");       // 1110
        potionAmplifiers.put(Potion.moveSpeed.getId(), "5");
        potionAmplifiers.put(Potion.digSpeed.getId(), "5");
        potionAmplifiers.put(Potion.damageBoost.getId(), "5");
        potionAmplifiers.put(Potion.regeneration.getId(), "5");
        potionAmplifiers.put(Potion.harm.getId(), "5");
        potionAmplifiers.put(Potion.heal.getId(), "5");
        potionAmplifiers.put(Potion.resistance.getId(), "5");
        potionAmplifiers.put(Potion.poison.getId(), "5");
        potionAmplifiers.put(Potion.jump.getId(), "5");
        potionAmplifiers.put(Potion.featherFalling.getId(), "5");
    }
}