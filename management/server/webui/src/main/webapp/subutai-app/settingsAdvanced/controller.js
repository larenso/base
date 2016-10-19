"use strict";

angular.module("subutai.settings-advanced.controller", [])
    .controller("SettingsAdvancedCtrl", SettingsAdvancedCtrl)
    .config(['terminalConfigurationProvider', function (terminalConfigurationProvider) {
        terminalConfigurationProvider.config('modern').allowTypingWriteDisplaying = false;
        terminalConfigurationProvider.config('modern').outputDelay = 80;
    }]);

SettingsAdvancedCtrl.$inject = ["$scope", "SettingsAdvancedSrv", "SweetAlert", "$sce", "cfpLoadingBar"];

function SettingsAdvancedCtrl($scope, SettingsAdvancedSrv, SweetAlert, $sce, cfpLoadingBar) {

    cfpLoadingBar.start();
    angular.element(document).ready(function () {
        cfpLoadingBar.complete();
    });

    var vm = this;
    vm.config = {};
    vm.karafLogs = '';
    vm.logLevel = 'all';
    vm.activeTab = "karaflogs";
    vm.admin = false;
    vm.getConfig = getConfig;
    vm.updateConfig = updateConfig;
    vm.saveLogs = saveLogs;
    vm.renderHtml = renderHtml;
    vm.setLevel = setLevel;

    if (localStorage.getItem("currentUserPermissions"))
        for (var i = 0; i < localStorage.getItem("currentUserPermissions").length; i++) {
            if (localStorage.getItem("currentUserPermissions")[i] == 2) {
                vm.activeTab = "karafconsole";
                vm.admin = true;

                //Console UI
                $scope.theme = 'modern';
                setTimeout(function () {
                    $scope.$broadcast('terminal-output', {
                        output: true,
                        text: [
                            'Karaf Subutai',
                        ],
                        breakLine: true
                    });
                    $scope.prompt.path('/');
                    $scope.prompt.user('karaf');

                    /*$scope.results.splice(0, $scope.results.length);
                     $scope.$$phase || $scope.$apply();*/

                    $scope.$apply();

                    $('.terminal-viewport').perfectScrollbar();
                }, 100);
            }
        }


    $scope.session = {
        commands: [],
        output: []
    };

    $scope.$watchCollection(function () {
        return $scope.session.commands;
    }, function (n) {
        for (var i = 0; i < n.length; i++) {
            $scope.$broadcast('terminal-command', n[i]);
        }
        $scope.session.commands.splice(0, $scope.session.commands.length);
        $scope.$$phase || $scope.$apply();
    });

    $scope.$watchCollection(function () {
        return $scope.session.output;
    }, function (n) {
        for (var i = 0; i < n.length; i++) {
            $scope.$broadcast('terminal-output', n[i]);
        }
        $scope.session.output.splice(0, $scope.session.output.length);
        $scope.$$phase || $scope.$apply();
    });

    $scope.$on('terminal-input', function (e, consoleInput) {
        var output = [];
        $scope.outputDelay = 0;

        $scope.showPrompt = false;

        var cmd = consoleInput[0];

        try {
            if (cmd.command == 'clear') {
                $scope.results.splice(0, $scope.results.length);
                $scope.$$phase || $scope.$apply();
                return;
            }

            SettingsAdvancedSrv.sendCommand(cmd.command).success(function (data) {
                if (data.length > 0) {
                    if (cmd.command.includes("help")) {
                        data = data.replaceAll("1m", "");
                    }
                    output = data.split('\r');
                }

                $scope.session.output.push(
                    {output: true, text: output, breakLine: true}
                );
            }).error(function (data) {
                $scope.session.output.push({output: true, breakLine: true, text: [data]});
            });

        } catch (err) {
            $scope.session.output.push({output: true, breakLine: true, text: [err.message]});
        }
    });
    //END Console UI

    function getConfig() {
        $('.js-karaflogs-load-screen').show();
        SettingsAdvancedSrv.getConfig().success(function (data) {
            $('.js-karaflogs-load-screen').hide();
            vm.config = data;
            vm.karafLogs = getFilteredLogs(data.karafLogs);
            setTimeout(function () {
                var codeBlock = document.getElementById('js-highlight-block');
                codeBlock.scrollTop = codeBlock.scrollHeight;
            }, 300);
        }).error(function (error) {
            SweetAlert.swal("ERROR!", error, "error");
            $('.js-karaflogs-load-screen').hide();
        });
    }

    //getConfig();

    function updateConfig() {
        SettingsAdvancedSrv.updateConfig(vm.config).success(function (data) {
            SweetAlert.swal("Success!", "Your settings were saved.", "success");
        }).error(function (error) {
            SweetAlert.swal("ERROR!", error, "error");
        });
    }

    function saveLogs() {
        var text = vm.config.karafLogs;
        var blob = new Blob([text], {type: "text/plain;charset=utf-8"});
        saveAs(blob, "karaflogs" + moment().format('YYYY-MM-DD HH:mm:ss') + ".txt");
    }

    var tagsToReplace = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;'
    };

    function replaceTag(tag) {
        return tagsToReplace[tag] || tag;
    }

    function renderHtml(html_code) {
        return $sce.trustAsHtml(html_code);
    }

    function setLevel() {
        vm.karafLogs = getFilteredLogs(vm.config.karafLogs);
    }

    function getFilteredLogs(html_code) {
        html_code = html_code.replace(/[&<>]/g, replaceTag);
        if (html_code && html_code.length > 0) {
            var html_code_array = html_code.match(/[^\r\n]+/g);
            var temp = false;
            var stingColor = false;
            for (var i = 0; i < html_code_array.length; i++) {
                if (vm.logLevel == 'all' || html_code_array[i].includes(vm.logLevel)) {
                    if (html_code_array[i].includes('ERROR') || html_code_array[i].includes('WARN')) {
                        if (html_code_array[i].includes('WARN')) {
                            stingColor = '#f1c40f';
                        } else if (html_code_array[i].includes('ERROR')) {
                            stingColor = '#c1272d';
                        }
                        html_code_array[i] = '<span style="color: ' + stingColor + ';">' + html_code_array[i] + '</span>';
                    } else {
                        stingColor = false;
                    }
                    temp = checkNextString(i, html_code_array, stingColor);
                    html_code_array = temp.array;
                    i = temp.index;
                } else {
                    html_code_array.splice(i, 1);
                    i--;
                }
            }
            return html_code_array.join('\n');
        } else {
            return '';
        }
    }

    function checkNextString(index, stringArray, color) {
        if (color == undefined || color == null) color = false;
        if (stringArray[index + 1] != undefined && !parseDate(stringArray[index + 1])) {
            index++;
            if (color) {
                stringArray[index] = '<span style="color: ' + color + ';">' + stringArray[index] + '</span>';
            }
            return checkNextString(index, stringArray, color);
        } else {
            return {"index": index, "array": stringArray};
        }
    }

    function parseDate(str) {
        var m = str.match(/[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1]) (2[0-3]|[01][0-9]):[0-5][0-9].*/);
        return (m) ? true : false;
    }
}

String.prototype.replaceAll = function (search, replacement) {
    var target = this;
    return target.split(search).join(replacement);
};

