#!/usr/bin/env bash

ZINGG_ENV_SH="zingg-env.sh"
export ZINGG_CONF_DIR="$(dirname "$0")"/../config

ZINGG_ENV_SH="${ZINGG_CONF_DIR}/${ZINGG_ENV_SH}"
if [[ -f "${ZINGG_ENV_SH}" ]]; then
  # Promote all variable declarations to environment (exported) variables
  set -a
  . ${ZINGG_ENV_SH}
  set +a
fi
