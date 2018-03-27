import java.io.File

import com.thoughtworks.dbmigrator.plugin.repository.Delta

//Delta(new File("/Users/syedatifakhtar/tmp/1#blah#0000.sql"))

"1#blah#0000.sql".split("#")(2).split(".sql")(0)