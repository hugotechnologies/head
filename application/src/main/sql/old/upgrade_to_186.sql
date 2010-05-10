-- fix index, it must be unique
DROP INDEX GLCODE_VALUE_IDX ON GL_CODE;
CREATE UNIQUE INDEX GLCODE_VALUE_IDX ON GL_CODE (GLCODE_VALUE);
  
UPDATE DATABASE_VERSION SET DATABASE_VERSION = 186 WHERE DATABASE_VERSION = 185;