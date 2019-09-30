#!/bin/bash
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# ARG_OPTIONAL_BOOLEAN([build_agent],[],[no comment],[off])
# ARG_OPTIONAL_BOOLEAN([build_scenario],[],[no comment],[off])
# ARG_OPTIONAL_SINGLE([agent_home],[],[no comment])
# ARG_OPTIONAL_SINGLE([parallel_run_size],[],[The size of running testcase at the same time],[1])
# ARG_POSITIONAL_INF([scenarios],[The scenario that you want to running])
# DEFINE_SCRIPT_DIR([scenarios_home],[SCENARIO HOME])_DEFINE_SCRIPT_DIR([scenarios_home],[cd "$(dirname "${BASH_SOURCE[0]}")" && pwd])
# ARG_HELP([The general script's help msg])
# ARGBASH_GO()
# needed because of Argbash --> m4_ignore([
### START OF CODE GENERATED BY Argbash v2.8.1 one line above ###
# Argbash is a bash code generator used to get arguments parsing right.
# Argbash is FREE SOFTWARE, see https://argbash.io for more info


die()
{
	local _ret=$2
	test -n "$_ret" || _ret=1
	test "$_PRINT_HELP" = yes && print_help >&2
	echo "$1" >&2
	exit ${_ret}
}


begins_with_short_option()
{
	local first_option all_short_options='h'
	first_option="${1:0:1}"
	test "$all_short_options" = "${all_short_options/$first_option/}" && return 1 || return 0
}

# THE DEFAULTS INITIALIZATION - POSITIONALS
_positionals=()
_arg_scenarios=()
# THE DEFAULTS INITIALIZATION - OPTIONALS
_arg_build_agent="off"
_arg_build_scenario="off"
_arg_agent_home=
_arg_parallel_run_size="1"


print_help()
{
	printf '%s\n' "The general script's help msg"
	printf 'Usage: %s [--(no-)build_agent] [--(no-)build_scenario] [--agent_home <arg>] [--parallel_run_size <arg>] [-h|--help] [<scenarios-1>] ... [<scenarios-n>] ...\n' "$0"
	printf '\t%s\n' "<scenarios>: The scenario that you want to running"
	printf '\t%s\n' "--build_agent, --no-build_agent: no comment (off by default)"
	printf '\t%s\n' "--build_scenario, --no-build_scenario: no comment (off by default)"
	printf '\t%s\n' "--agent_home: no comment (no default)"
	printf '\t%s\n' "--parallel_run_size: The size of running testcase at the same time (default: '1')"
	printf '\t%s\n' "-h, --help: Prints help"
}


parse_commandline()
{
	_positionals_count=0
	while test $# -gt 0
	do
		_key="$1"
		case "$_key" in
			--no-build_agent|--build_agent)
				_arg_build_agent="on"
				test "${1:0:5}" = "--no-" && _arg_build_agent="off"
				;;
			--no-build_scenario|--build_scenario)
				_arg_build_scenario="on"
				test "${1:0:5}" = "--no-" && _arg_build_scenario="off"
				;;
			--agent_home)
				test $# -lt 2 && die "Missing value for the optional argument '$_key'." 1
				_arg_agent_home="$2"
				shift
				;;
			--agent_home=*)
				_arg_agent_home="${_key##--agent_home=}"
				;;
			--parallel_run_size)
				test $# -lt 2 && die "Missing value for the optional argument '$_key'." 1
				_arg_parallel_run_size="$2"
				shift
				;;
			--parallel_run_size=*)
				_arg_parallel_run_size="${_key##--parallel_run_size=}"
				;;
			-h|--help)
				print_help
				exit 0
				;;
			-h*)
				print_help
				exit 0
				;;
			*)
				_last_positional="$1"
				_positionals+=("$_last_positional")
				_positionals_count=$((_positionals_count + 1))
				;;
		esac
		shift
	done
}


assign_positional_args()
{
	local _positional_name _shift_for=$1
	_positional_names=""
	_our_args=$((${#_positionals[@]} - 0))
	for ((ii = 0; ii < _our_args; ii++))
	do
		_positional_names="$_positional_names _arg_scenarios[$((ii + 0))]"
	done

	shift "$_shift_for"
	for _positional_name in ${_positional_names}
	do
		test $# -gt 0 || break
		eval "$_positional_name=\${1}" || die "Error during argument parsing, possibly an Argbash bug." 1
		shift
	done
}

parse_commandline "$@"
assign_positional_args 1 "${_positionals[@]}"

# OTHER STUFF GENERATED BY Argbash
scenarios_home="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)" || die "Couldn't determine the script's running directory, which probably matters, bailing out" 2

### END OF CODE GENERATED BY Argbash (sortof) ### ])
# [ <-- needed because of Argbash
#

home="$(cd "$(dirname $0)"; pwd)"

mvnw=${home}/../../mvnw
agent_home=${home}"/../../skywalking-agent"
scenarios_home="${home}/scenarios"

workspace="${home}/workspace"
task_state_house="${workspace}/.states"

plugin_autotest_helper="${home}/dist/plugin-autotest-helper.jar"

prepareAndClean() {
  echo "prepare and clear"
  if [[ -f ${task_state_house} ]]; then
    rm -f ${task_state_house}/* > /dev/null
  else
    mkdir -p ${workspace}/.states
  fi

  [[ -f ${workspace}/testcases ]] && rm -rf ${workspace}/testcases
  mkdir -p ${workspace}/testcases

  if [[ ${#_arg_scenarios[@]} -lt 1 ]]; then
    _arg_scenarios=`ls ./scenarios/|sed -e "s/\t/\n/g"`
  fi

  # docker prune
  docker container prune -f
  docker network   prune -f
  docker volume    prune -f
  docker image     prune -f

  # build plugin/test
  ${mvnw} clean package -DskipTests docker:build
  if [[ ! -f ${plugin_autotest_helper} ]]; then
    echo -e "\033[31mplugin/test build failure\033[0m" # ]]
    exit 1;
  fi
}

waitForAvailable() {
  if [[ `ls -l ${task_state_house} |grep -c FAILURE` -gt 0 ]]; then
    exit 1
  fi
  while [[ `ls -l ${task_state_house} |grep -c RUNNING` -gt ${_arg_parallel_run_size} ]]
  do
    if [[ `ls -l ${task_state_house} |grep -c FAILURE` -gt 0 ]]; then
      exit 1
    fi
    sleep 2
  done
}

################################################
start_stamp=`date +%s`

prepareAndClean ## prepare to start

echo "start submit job"
num_of_scenarios=0
for scenario_name in ${_arg_scenarios}
do
  scenario_home=${scenarios_home}/${scenario_name} && cd ${scenario_home}

  supported_version_file=${scenario_home}/support-version.list
  if [[ ! -f $supported_version_file ]]; then
    echo -e "\033[31m[ERROR] cannot found 'support-version.list' in directory ${scenario_name}\033[0m" # to escape ]]
    continue
  fi

  echo "scenario.name=${scenario_name}"
  num_of_scenarios=$((num_of_scenarios+1))
  # [[ -f ${testcases_home}/configuration.yml ]] && rm -f ${testcases_home}/configuration.yml
  # cp -f ${scenario_home}/configuration.yml ${testcases_home} > /dev/null
  #
  # testcases_home=${home}/testcases/${scenario_name} && mkdir -p ${testcases_home}

  supported_versions=`grep -v -E "^$|^#" ${supported_version_file}`
  for version in ${supported_versions}
  do
    testcase_name="${scenario_name}-${version}"

    # testcase working directory, there are logs, reports, and packages.
    case_work_base=${workspace}/testcases/${scenario_name}/${testcase_name}
    mkdir -p ${case_work_base}/{data,packages,logs,reports}

    case_work_logs_dir=${case_work_base}/logs

    # copy expectedData.yml
    cp ./config/expectedData.yaml ${case_work_base}/data

    echo "build ${testcase_name}"
    ${mvnw} clean package -P${testcase_name} > ${case_work_logs_dir}/build.log

    mv ./target/${scenario_name}.war ${case_work_base}/packages

    java -Dconfigure.file=${scenario_home}/configuration.yml \
        -Dscenario.home=${case_work_base} \
        -Dscenario.name=${scenario_name} \
        -Dscenario.version=${version} \
        -Doutput.dir=${case_work_base} \
        -Dagent.dir=${agent_home} \
        -jar ${plugin_autotest_helper} 1>${case_work_logs_dir}/helper.log 2>&1

    [[ $? -ne 0 ]] && echo -e "\033[31m[ERROR] ${testcase_name}, generate script failure! \033[0m" && continue # ]]

    waitForAvailable
    echo "start container of testcase.name=${testcase_name}"
    bash ${case_work_base}/scenario.sh ${task_state_house} 1>${case_work_logs_dir}/${testcase_name}.log 2>&1 &
  done

  echo -e "\033[33m${scenario_name} has already sumbitted\033[0m" # to escape ]]
done

# wait to finish
while [[ `ls -l ${task_state_house} |grep -c RUNNING` -gt 0 ]]; do
  sleep 1
done

if [[ `ls -l ${task_state_house} |grep -c FAILURE` -gt 0 ]]; then
  exit 1
fi

elapsed=$(( `date +%s` - $start_stamp ))
num_of_testcases="`ls -l ${task_state_house} |grep -c FINISH`"

printf "Scenarios: %d, Testcases: %d, parallel_run_size: %d, Elapsed: %02d:%02d:%02d \n" \
  ${num_of_scenarios} "${num_of_testcases}" "${_arg_parallel_run_size}" \
  $(( ${elapsed}/3600 )) $(( ${elapsed}%3600/60 )) $(( ${elapsed}%60 ))

#
# ] <-- needed because of Argbash
