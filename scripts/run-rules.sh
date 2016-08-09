#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $SCRIPT_DIR/ENV.sh

if [ $# -ne 7 ]
then
  echo "Usage: MAVEN KB_URL KB_NAME KB_USER KB_PASS KB_DATA_DIR RULETYPE"
  echo $@
  exit 1
fi

MAVEN=${1:?}
KB_URL=${2:?}
KB=${3:?}
KB_USER=${4:?}
KB_PASS=${5:?}
KB_DATA_DIR=${6:?}
RULETYPE=${7:?}

OUTPUTDIR=$KB_DATA_DIR/$RULETYPE/
RULES_RES_DIR=$RULETYPE/

echo Running rules
mkdir -p $OUTPUTDIR
$MAVEN \
    --debug \
    --file scripts/pom.xml \
    -Dclojure.vmargs="-d64 -Xmx2G -XX:MaxPermSize=256m" \
    -Dclojure.mainClass="edu.ucdenver.ccp.kabob.build.run_rules" \
    -Dclojure.args="$KB_URL $KB $KB_USER $KB_PASS $OUTPUTDIR $RULES_RES_DIR" \
    clojure:run
# Ensure that all subdirectories implicit in the `RULETYPE` are accessible to
# later processes.
find $KB_DATA_DIR -type d -exec chmod 0755 {} \;
