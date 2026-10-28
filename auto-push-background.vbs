Set objFSO = CreateObject("Scripting.FileSystemObject")
strCurrentDir = objFSO.GetParentFolderName(WScript.ScriptFullName)
strScriptPath = strCurrentDir & "\scripts\auto-push.ps1"

Set WshShell = CreateObject("WScript.Shell")
WshShell.Run "powershell.exe -NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File """ & strScriptPath & """", 0, False
