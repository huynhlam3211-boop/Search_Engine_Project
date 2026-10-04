package com.vnsearch.index;

import java.lang.reflect.Array;
import java.util.Arrays;

public record Posting(int docId, int termFrequency, int[] positions){

    private static  final int[] NO_POSITIONS = new int[0];

    public Posting{
        if (positions==null){
            positions=NO_POSITIONS;
        }
    }

    public int positionsCount(){
        return positions.length;

    }

    @Override 
    public boolean equals( Object ortherObject){
        if (this == other){
            return true;
        }
        return other instanceof Posting that 
        && docId == that.docId
        && termFrequency == that.termFrequency
        && Arrays.equals(positions, that.positions);

    }

    @Override
    public int hashCode(){
        return 31 * (31 *docId +termFrequency) + Arrays.hashCode(positions);
    }



}