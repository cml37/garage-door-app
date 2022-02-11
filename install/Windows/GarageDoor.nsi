;GarageDoor Windows Client installer
;Written by Rich West 01/27/2006
;Updated by Chris Lenderman 2/11/2022

SetCompressor /SOLID lzma

;--------------------------------
;Interface Settings

	XPStyle on

	; Add any additional plugins needed by this script
        !addplugindir NullSoftPlugins

	; Modern UI
	!include "MUI.nsh"
        !include nsdialogs.nsh
        !include x64.nsh
        !include "WordFunc.nsh"

	!define MUI_ABORTWARNING

	!define MUI_STARTMENUPAGE_REGISTRY_ROOT HKLM
	!define MUI_STARTMENUPAGE_REGISTRY_KEY "SOFTWARE\GarageDoor"
	!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "Start Menu"

        !define MUI_FINISHPAGE_RUN
	!define MUI_FINISHPAGE_RUN_FUNCTION "LaunchLink"
	!define MUI_FINISHPAGE_RUN_TEXT "Start and Configure GarageDoor"

	!define WNDCLASS "GarageDoor"
	!define TIMEOUT 2000
	!define SYNC_TERM 0x00100001

	!define UNINSTALL_KEY "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\GarageDoor"
	!define AUTORUN_KEY "SOFTWARE\Microsoft\Windows\CurrentVersion\Run"
	!define INSTALLER_KEY "SOFTWARE\GarageDoor"

;--------------------------------
!macro TerminateApp
	Push $0 ; window handle
	Push $1
	Push $2 ; process handle

loop:
	FindWindow $0 "${WNDCLASS}" ""
	IntCmp $0 0 done

	System::Call 'user32.dll::GetWindowThreadProcessId(i r0, *i .r1) i .r2'
	System::Call 'kernel32.dll::OpenProcess(i ${SYNC_TERM}, i 0, i r1) i .r2'
	SendMessage $0 ${WM_CLOSE} 0 0 /TIMEOUT=${TIMEOUT}

	System::Call 'kernel32.dll::WaitForSingleObject(i r2, i ${TIMEOUT}) i .r1'
	IntCmp $1 0 close

	System::Call 'kernel32.dll::TerminateProcess(i r2, i 0) i .r1'

close:
	System::Call 'kernel32.dll::CloseHandle(i r2) i .r1'
	goto loop

done:
	Pop $2
	Pop $1
	Pop $0
!macroend

;--------------------------------
!macro CheckUserRights
	ClearErrors
	UserInfo::GetName
	IfErrors good
	Pop $0
	UserInfo::GetAccountType
	Pop $1
	StrCmp $1 "Admin" good
	StrCmp $1 "Power" good

	MessageBox MB_OK "Administrative rights are required."
	Abort

good:
!macroend

;--------------------------------
Function .onInit
	!insertmacro CheckUserRights
FunctionEnd

;--------------------------------
Function un.onInit
	!insertmacro CheckUserRights
FunctionEnd

;--------------------------------
;General

	Name "GarageDoor"
	OutFile "GarageDoor-Win32-0.1.0.exe"

	; Default installation folder
	InstallDir "$PROGRAMFILES\GarageDoor"

	; Get installation folder from registry if available
	InstallDirRegKey HKLM "${INSTALLER_KEY}" ""

;--------------------------------
;Pages

	Var STARTMENU_FOLDER

	!insertmacro MUI_PAGE_WELCOME
	!insertmacro MUI_PAGE_LICENSE "..\..\license.txt"
	!insertmacro MUI_PAGE_DIRECTORY
	!insertmacro MUI_PAGE_STARTMENU Application $STARTMENU_FOLDER
	!insertmacro MUI_PAGE_INSTFILES
	!insertmacro MUI_PAGE_FINISH

	!insertmacro MUI_UNPAGE_WELCOME
	!insertmacro MUI_UNPAGE_CONFIRM
	!insertmacro MUI_UNPAGE_INSTFILES
	!insertmacro MUI_UNPAGE_FINISH

;--------------------------------
;Languages
 
	!insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer Sections

Section "GarageDoor (Required)" SecGarageDoor
	!insertmacro TerminateApp

	SectionIn RO

	SetOutPath "$INSTDIR"
	SetShellVarContext all

	; Put file there
	File "..\..\build\libs\garage-door-app-all.jar"
	File "..\..\CONTRIBUTORS.txt"
	File "..\..\CHANGES.txt"
	File "GarageDoor.ico"
	
	; Write the installation path into the registry
	WriteRegStr HKLM "${INSTALLER_KEY}" "" "$INSTDIR"

	; Run at logon for all users
	WriteRegStr HKLM "${AUTORUN_KEY}" "GarageDoor" "$INSTDIR\garage-door-app-all.jar"

	; Write the uninstall keys for Windows
	WriteRegStr HKLM "${UNINSTALL_KEY}" "DisplayName" "GarageDoor"
	WriteRegStr HKLM "${UNINSTALL_KEY}" "UninstallString" '"$INSTDIR\uninstall.exe"'
	WriteRegDWORD HKLM "${UNINSTALL_KEY}" "NoModify" 1
	WriteRegDWORD HKLM "${UNINSTALL_KEY}" "NoRepair" 1
	
	; Create uninstaller
	WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd

; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"

	!insertmacro MUI_STARTMENU_WRITE_BEGIN Application
	 
	; Create shortcuts
	CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER"
	CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\GarageDoor.lnk" "$INSTDIR\garage-door-app-all.jar" "" "$INSTDIR\GarageDoor.ico" 0
	CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Contributors.lnk" "$INSTDIR\CONTRIBUTORS.txt"
	CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\History.lnk" "$INSTDIR\CHANGES.txt"
	CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Uninstall.lnk" "$INSTDIR\Uninstall.exe"

	!insertmacro MUI_STARTMENU_WRITE_END
	
SectionEnd

Function LaunchLink
  ExecShell "" "$SMPROGRAMS\$STARTMENU_FOLDER\GarageDoor.lnk"
FunctionEnd

;--------------------------------
;Descriptions

	; Language strings
	LangString DESC_SecGarageDoor ${LANG_ENGLISH} "GarageDoor is a program for controlling your garage door directly from your desktop."
 
;--------------------------------
;Uninstaller Section

Section "Uninstall"

	!insertmacro TerminateApp

	SetShellVarContext all

	!insertmacro MUI_STARTMENU_GETFOLDER Application $STARTMENU_FOLDER

	ReadRegStr $INSTDIR HKLM "${INSTALLER_KEY}" ""

	; Remove shortcuts
	RMDir /r "$SMPROGRAMS\$STARTMENU_FOLDER"

	; Remove files
	RMDir /r "$INSTDIR"

	; Remove registry keys
	DeleteRegValue HKLM "${AUTORUN_KEY}" "GarageDoor"
	DeleteRegKey HKLM "${UNINSTALL_KEY}"
	DeleteRegKey HKLM "${INSTALLER_KEY}"

SectionEnd
