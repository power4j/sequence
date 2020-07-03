package com.power4j.kit.seq.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 二元组
 *
 * @author CJ (jclazz@outlook.com)
 * @date 2020/7/2
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class Pair<L,R> {
    private L left;
    private R right;
    private static final Pair<?,?> NULL_VAL = new Pair<>(null,null);

    public static <L,R> Pair<L,R> of(L left,R right){
        return new Pair<>(left,right);
    }

    public static <L,R> Pair<L,R> nothing(){
        return (Pair<L,R>)NULL_VAL;
    }
}
