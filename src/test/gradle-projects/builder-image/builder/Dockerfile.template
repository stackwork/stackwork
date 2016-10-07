FROM ${project.project(':cassandra').stackwork.imageId}
COPY init-database.cql /
CMD ["cqlsh", "-f", "init-database.cql", "cassandra"]
