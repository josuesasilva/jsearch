#!/bin/sh

# MAP e P BM25
./release/trec_eval release/WT10g.qrels release/BM25.out > result/output.txt > result/output.txt
echo "\n---\n" >> result/output.txt

# NDCG BM25
echo "ndcg BM25" >> result/output.txt
./release/trec_eval -q -c -m ndcg.0=0,1=1,2=3 release/WT10g.qrels release/BM25.out >> result/output.txt
echo "\n---\n" >> result/output.txt

# MAP e P Rocchio
./release/trec_eval release/WT10g.qrels release/Rocchio.out >> result/output.txt
echo "\n---\n" >> result/output.txt

# NDCG Rocchio
echo "ndcg Rocchio" >> result/output.txt
./release/trec_eval -q -c -m ndcg.0=0,1=1,2=3 release/WT10g.qrels release/Rocchio.out >> result/output.txt
echo "\n---\n" >> result/output.txt

# MAP e P QueryExpansion
./release/trec_eval release/WT10g.qrels release/QueryExpansion.out >> result/output.txt
echo "\n---\n" >> result/output.txt

# NDCG QueryExpansion
echo "ndcg QueryExpansion" >> result/output.txt
./release/trec_eval -q -c -m ndcg.0=0,1=1,2=3 release/WT10g.qrels release/QueryExpansion.out >> result/output.txt
echo "\n---\n" >> result/output.txt
