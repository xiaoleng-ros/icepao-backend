package com.leng.ice.utils;

import java.util.*;
import java.util.function.Function;

/**
 * TOP N 优化工具类
 * 使用优先队列（最小堆）优化TOP N运算
 * 
 * @author leng
 */
public class TopNOptimizer {

    /**
     * 使用优先队列获取TOP N元素
     * 时间复杂度：O(n log k)，其中n是总元素数，k是需要的TOP N数量
     * 空间复杂度：O(k)
     * 
     * @param elements 待处理的元素列表
     * @param scoreFunction 计算分数的函数（分数越小越好）
     * @param topN 需要获取的TOP N数量
     * @param <T> 元素类型
     * @return TOP N元素列表，按分数从小到大排序
     */
    public static <T> List<ScoredElement<T>> getTopNWithMinHeap(List<T> elements, 
                                                                Function<T, Long> scoreFunction, 
                                                                int topN) {
        if (elements == null || elements.isEmpty() || topN <= 0) {
            return new ArrayList<>();
        }
        
        // 使用最大堆来维护最小的topN个元素
        // 堆顶是当前topN中分数最大的元素
        PriorityQueue<ScoredElement<T>> maxHeap = new PriorityQueue<>(
            (a, b) -> Long.compare(b.getScore(), a.getScore())
        );
        
        for (T element : elements) {
            long score = scoreFunction.apply(element);
            ScoredElement<T> scoredElement = new ScoredElement<>(element, score);
            
            if (maxHeap.size() < topN) {
                // 堆未满，直接添加
                maxHeap.offer(scoredElement);
            } else if (score < maxHeap.peek().getScore()) {
                // 当前元素分数更小（更好），替换堆顶
                maxHeap.poll();
                maxHeap.offer(scoredElement);
            }
        }
        
        // 将堆中元素转换为列表，并按分数从小到大排序
        List<ScoredElement<T>> result = new ArrayList<>(maxHeap);
        result.sort(Comparator.comparingLong(ScoredElement::getScore));
        
        return result;
    }
    
    /**
     * 带分数的元素包装类
     */
    public static class ScoredElement<T> {
        private final T element;
        private final long score;
        
        public ScoredElement(T element, long score) {
            this.element = element;
            this.score = score;
        }
        
        public T getElement() {
            return element;
        }
        
        public long getScore() {
            return score;
        }
    }
}