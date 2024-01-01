mkdir -p /opt/jdk
cd /opt/jdk
wget https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz
tar -zxvf jdk-21_linux-x64_bin.tar.gz
set -i '$a #jdk' ~/.bash_profile
set -i '$a export JAVA_HOME=/opt/jdk/jdk-21.0.1' ~/.bash_profile
set -i '$a export PATH=$PATH:$HOME/bin:$JAVA_HOME/bin' ~/.bash_profile
source ~/.bash_profile