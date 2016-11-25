#!/bin/sh

# MAP e P BM25
./release/trec_eval release/en.qrels release/BM25.out > result/output.txt
echo "\n---\n" >> result/output.txt

# # NDCG BM25
# echo "ndcg BM25" >> result/output.txt
# ./release/trec_eval -q -c -m ndcg.0=0,1=1,2=3 release/en.qrels release/BM25.out >> result/output.txt
# echo "\n---\n" >> result/output.txt

# # MAP e P Rocchio
# ./release/trec_eval release/en.qrels release/Rocchio.out >> result/output.txt
# echo "\n---\n" >> result/output.txt

# # NDCG Rocchio
# echo "ndcg Rocchio" >> result/output.txt
# ./release/trec_eval -q -c -m ndcg.0=0,1=1,2=3 release/en.qrels release/Rocchio.out >> result/output.txt
# echo "\n---\n" >> result/output.txt

# MAP e P QueryExpansion
./release/trec_eval release/en.qrels release/QueryExpansion.out >> result/output.txt
echo "\n---\n" >> result/output.txt

# # NDCG QueryExpansion
# echo "ndcg QueryExpansion" >> result/output.txt
# ./release/trec_eval -q -c -m ndcg.0=0,1=1,2=3 release/en.qrels release/QueryExpansion.out >> result/output.txt
# echo "\n---\n" >> result/output.txt



# echo "ndcg BM25" >> result/output.txt
# ./release/trec_eval -q release/en.qrels release/BM25.out > result/output-query.txt
# ./release/trec_eval -q -c -m ndcg.0=0,1=1,2=3 release/en.qrels release/BM25.out >> result/output-query.txt


# echo "ndcg Rocchio" >> result/output.txt
# ./release/trec_eval -q release/en.qrels release/Rocchio.out >> result/output-query.txt
# ./release/trec_eval -q -c -m ndcg.0=0,1=1,2=3 release/en.qrels release/Rocchio.out >> result/output-query.txt

# echo "ndcg QueryExpansion" >> result/output-query.txt
# ./release/trec_eval -q release/en.qrels release/QueryExpansion.out >> result/output-query.txt
# ./release/trec_eval -q -c -m ndcg.0=0,1=1,2=3 release/en.qrels release/QueryExpansion.out >> result/output-query.txt
