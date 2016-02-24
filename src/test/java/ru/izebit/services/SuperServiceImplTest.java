package ru.izebit.services;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class SuperServiceImplTest {

    private int level;
    private int index;
    private Double correctResult;

    public SuperServiceImplTest(int level, int index, Double correctResult) {
        this.correctResult = correctResult;
        this.index = index;
        this.level = level;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Collection<Object[]> data = new ArrayList<>();
        data.add(new Object[]{0, 0, 0.0});
        data.add(new Object[]{1, 2, null});
        data.add(new Object[]{3, 3, 43.75});
        data.add(new Object[]{3, 0, 43.75});
        data.add(new Object[]{3, 2, 106.25});
        data.add(new Object[]{3, 1, 106.25});

        return data;
    }


    @Test
    public void getHumanEdgeWeightTest2() throws Exception {
        SuperServiceImpl superService = new SuperServiceImpl();
        superService.setCache(new ResultCacheImpl<>());

        Double result = superService.getHumanEdgeWeight(this.level, this.index);
        if (this.correctResult == null) Assert.assertNull(result);
        else Assert.assertEquals(this.correctResult, result, 0.5);
    }
}
