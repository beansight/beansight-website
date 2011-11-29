#!/bin/sh
# clean up the generated javadoc to include it in the reference wiki
#
# copy paste after: <!-- ============ METHOD DETAIL ========== -->
# up to: 			<!-- ========= END OF CLASS DATA ========= -->

sed -i 's/<!-- -->//g' ./APIInsights.html
sed -i 's/java.lang.//g' ./APIInsights.html
sed -i 's/public static void //g' ./APIInsights.html
