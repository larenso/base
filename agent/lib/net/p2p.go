package net

import (
	"bufio"
	"fmt"
	"io/ioutil"
	"os"
	"os/exec"
	"strings"
	"subutai/config"
	"subutai/log"
)

func p2pFile(line string) {
	path := config.Agent.DataPrefix + "/var/subutai-network/"
	file := path + "p2p.txt"
	if _, err := os.Stat(path); os.IsNotExist(err) {
		log.Check(log.FatalLevel, "create "+path+" folder", os.MkdirAll(path, 0755))
	}
	if _, err := os.Stat(file); os.IsNotExist(err) {
		_, err = os.Create(file)
		log.Check(log.FatalLevel, "Creating "+file, err)
	}

	f, err := os.OpenFile(file, os.O_APPEND|os.O_WRONLY, 0600)
	log.Check(log.FatalLevel, "Opening file for append "+file, err)
	defer f.Close()
	_, err = f.WriteString(line + "\n")
	log.Check(log.FatalLevel, "Opening file for append "+file, err)
}

func CreateP2PTunnel(interfaceName, communityName, localPeepIPAddr string) {
	p2pFile(interfaceName + " " + localPeepIPAddr + " " + communityName[0:32] + " " + communityName)
	log.Check(log.FatalLevel, "p2p command: ", exec.Command("p2p", "-start", "-key", communityName[0:32], "-dev", interfaceName, "-ip", localPeepIPAddr, "-hash", communityName).Start())
}

func PrintN2NTunnels() {
	fmt.Println("LocalPeerIP\tLocalInterface\tCommunity")

	file, err := os.Open(config.Agent.DataPrefix + "/var/subutai-network/p2p.txt")
	log.Check(log.FatalLevel, "Opening p2p.txt", err)
	scanner := bufio.NewScanner(bufio.NewReader(file))
	for scanner.Scan() {
		line := strings.Fields(scanner.Text())
		if len(line) > 3 {
			fmt.Println(line[1] + "\t" + line[0] + "\t" + line[3])
		}
	}
	file.Close()
}

func RemoveP2PTunnel(communityName string) {
	log.Check(log.FatalLevel, "p2p command: ", exec.Command("p2p", "-stop", "-hash", communityName).Start())

	file, err := os.Open(config.Agent.DataPrefix + "/var/subutai-network/p2p.txt")
	log.Check(log.FatalLevel, "Opening p2p.txt", err)
	scanner := bufio.NewScanner(bufio.NewReader(file))
	newconf := ""
	for scanner.Scan() {
		line := scanner.Text()
		if !strings.HasSuffix(line, communityName) {
			newconf = newconf + line + "\n"
		}
	}
	file.Close()
	log.Check(log.FatalLevel, "Removing p2p tunnel", ioutil.WriteFile(config.Agent.DataPrefix+"/var/subutai-network/p2p.txt", []byte(newconf), 0644))
}