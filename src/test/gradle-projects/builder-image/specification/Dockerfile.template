FROM ${project.project(':cassandra').stackwork.imageId}
COPY query-data.cql /
COPY query-data.sh /
CMD ["bash", "query-data.sh"]
