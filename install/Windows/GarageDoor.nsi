;GarageDoor Windows Client installer
;Written by Rich West 01/27/2006
;Updated by Chris Lenderman 5/26/2017

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

	; Define the minimum needed JRE version, as well as "bundle" download links
	; for 32 bit and 64 bit version JREs. 
	; These change as new Java versions are created.  The below are for Java 1.8.
        !define JRE_VERSION "1.5"
        !define JRE_URL_32BIT "http://javadl.oracle.com/webapps/download/AutoDL?BundleId=218831_e9e7ea248e2c4826b92b3f075a80e441"
        !define JRE_URL_64BIT "http://javadl.oracle.com/webapps/download/AutoDL?BundleId=218833_e9e7ea248e2c4826b92b3f075a80e441"

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
!macro CUSTOM_PAGE_JREINFO
   Page custom CUSTOM_PAGE_JREINFO
!macroend

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
Function CUSTOM_PAGE_JREINFO

  push $0
  push $1
  push $2
  
  Push "${JRE_VERSION}"
  Call DetectJRE
  Pop $0
  Pop $1
  StrCmp $0 "OK" exit

  nsDialogs::create /NOUNLOAD 1018
  pop $1

  StrCmp $0 "0" NoFound
  StrCmp $0 "-1" FoundOld


NoFound:
  !insertmacro MUI_HEADER_TEXT "JRE Installation Required" "This application requires Java ${JRE_VERSION} or higher"
  ${NSD_CreateLabel} 0 0 100% 100% "This application requires installation of the Java Runtime Environment. This will be downloaded and installed as part of the installation."
  pop $1
  goto ShowDialog

FoundOld:
  !insertmacro MUI_HEADER_TEXT "JRE Update Required" "This application requires Java ${JRE_VERSION} or higher"
  ${NSD_CreateLabel} 0 0 100% 100% "This application requires a more recent version of the Java Runtime Environment. This will be downloaded and installed as part of the installation."
  pop $1
  goto ShowDialog

ShowDialog:

  nsDialogs::Show

exit:

  pop $2
  pop $1
  pop $0

FunctionEnd

;--------------------------------
; Checks to ensure that the installed version of the JRE (if any) is at least that of
; the JRE_VERSION variable.  The JRE will be downloaded and installed if necessary
; The full path of java.exe will be returned on the stack

Function DownloadAndInstallJREIfNecessary
  Push $0
  Push $1

  DetailPrint "Detecting JRE Version"
  Push "${JRE_VERSION}"
  Call DetectJRE
  Pop $0	; Get return value from stack
  Pop $1	; get JRE path (or error message)
  DetailPrint "JRE Version detection complete - result = $1"

  strcmp $0 "OK" End downloadJRE

downloadJRE:
   ${If} ${RunningX64}
       DetailPrint "About to download JRE from ${JRE_URL_64BIT}"
       Inetc::get "${JRE_URL_64BIT}" "$TEMP\jre_Setup.exe" /END
   ${Else}
       DetailPrint "About to download JRE from ${JRE_URL_32BIT}"
       Inetc::get "${JRE_URL_32BIT}" "$TEMP\jre_Setup.exe" /END
   ${EndIf}

  Pop $0 # return value = exit code, "OK" if OK
  DetailPrint "Download result = $0"

  strcmp $0 "OK" downloadsuccessful
  MessageBox MB_OK "There was a problem downloading required component - Error: $0"
  abort
downloadsuccessful:

  DetailPrint "Launching JRE setup"
  
  IfSilent doSilent
  ExecWait '"$TEMP\jre_setup.exe" /s REBOOT=Suppress /L \"$TEMP\jre_setup.log\"' $0
  goto jreSetupfinished
doSilent:
  ExecWait '"$TEMP\jre_setup.exe" /s REBOOT=Suppress /L \"$TEMP\jre_setup.log\"' $0
  
jreSetupFinished:
  DetailPrint "JRE Setup finished"
  Delete "$TEMP\jre_setup.exe"
  StrCmp $0 "0" InstallVerif 0
  Push "The JRE setup has been abnormally interrupted - return code $0"
  Goto ExitInstallJRE
 
InstallVerif:
  DetailPrint "Checking the JRE Setup's outcome"
  Push "${JRE_VERSION}"
  Call DetectJRE  
  Pop $0	  ; DetectJRE's return value
  Pop $1	  ; JRE home (or error message if compatible JRE could not be found)
  StrCmp $0 "OK" 0 JavaVerStillWrong
  Goto JREPathStorage
JavaVerStillWrong:
  Push "Unable to find JRE with version above ${JRE_VERSION}, even though the JRE setup was successful$\n$\n$1"
  Goto ExitInstallJRE
 
JREPathStorage:
  push $0	; => rv, r1, r0
  exch 2	; => r0, r1, rv
  exch		; => r1, r0, rv
  Goto End
 
ExitInstallJRE:
  Pop $1
  MessageBox MB_OK "Unable to install Java - Setup will be aborted$\n$\n$1"
  Pop $1 	; Restore $1
  Pop $0 	; Restore $0
  Abort
End:
  Pop $1	; Restore $1
  Pop $0	; Restore $0

FunctionEnd

;--------------------------------
; DetectJRE
; Inputs:  Minimum JRE version requested on stack (this value will be overwritten)
; Outputs: Returns two values on the stack: 
;     First value (rv0):  0 - JRE not found. -1 - JRE found but too old. OK - JRE found and meets version criteria
;     Second value (rv1):  Problem description.  Otherwise - Path to the java runtime (javaw.exe will be at .\bin\java.exe relative to this path)
 
Function DetectJRE

  Exch $0	; Get version requested  
		; Now the previous value of $0 is on the stack, and the asked for version of JDK is in $0
  Push $1	; $1 = Java version string (ie 1.5.0)
  Push $2	; $2 = Javahome
  Push $3	; $3 = holds the version comparison result

		; stack is now:  r3, r2, r1, r0

   ; Set our registry view to 64 bit if we are a 64 bit OS
   ${If} ${RunningX64}
      SetRegView 64
   ${EndIf}

  ; first, check for an installed JRE
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  StrCmp $1 "" DetectTry2
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"
  StrCmp $2 "" DetectTry2
  Goto GetJRE
 
DetectTry2:
  ; next, check for an installed JDK
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
  StrCmp $1 "" DetectTry3
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$1" "JavaHome"
  StrCmp $2 "" DetectTry3

DetectTry3:
  ; next, check for an installed JDK
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\JDK" "CurrentVersion"
  StrCmp $1 "" NoFound
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\JDK\$1" "JavaHome"
  StrCmp $2 "" NoFound
 
GetJRE:
  ; ok, we found a JRE, let's compare it's version and make sure it is new enough
; $0 = version requested. $1 = version found. $2 = javaHome
  IfFileExists "$2\bin\java.exe" 0 NoFound

  ${VersionCompare} $0 $1 $3 ; $3 now contains the result of the comparison
  DetailPrint "Comparing version $0 to $1 results in $3"
  intcmp $3 1 FoundOld
  goto FoundNew
 
NoFound:
  ; No JRE found
  strcpy $0 "0"
  strcpy $1 "No JRE Found"
  Goto DetectJREEnd
 
FoundOld:
  ; An old JRE was found
  strcpy $0 "-1"
  strcpy $1 "Old JRE found"
  Goto DetectJREEnd  
FoundNew:
  ; A suitable JRE was found 
  strcpy $0 "OK"
  strcpy $1 $2
  Goto DetectJREEnd

DetectJREEnd:
	; Restore our registry view to 32 bit
        SetRegView 32

	; at this stage, $0 contains rv0, $1 contains rv1
	; now, straighten the stack out and recover original values for r0, r1, r2 and r3
	; there are two return values: rv0 = -1, 0, OK and rv1 = JRE path or problem description
	; stack looks like this: 
                ;    r3,r2,r1,r0
	Pop $3	; => r2,r1,r0
	Pop $2	; => r1,r0
	Push $0 ; => rv0, r1, r0
	Exch 2	; => r0, r1, rv0
	Push $1 ; => rv1, r0, r1, rv0
	Exch 2	; => r1, r0, rv1, rv0
	Pop $1	; => r0, rv1, rv0
	Pop $0	; => rv1, rv0	
	Exch	; => rv0, rv1

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
	!insertmacro CUSTOM_PAGE_JREINFO
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

	call DownloadAndInstallJREIfNecessary

	SectionIn RO

	SetOutPath "$INSTDIR"
	SetShellVarContext all

	; Put file there
	File "..\GarageDoor.jar"
	File "..\..\CONTRIBUTORS.txt"
	File "..\..\CHANGES.txt"
	File "GarageDoor.ico"
	
	; Write the installation path into the registry
	WriteRegStr HKLM "${INSTALLER_KEY}" "" "$INSTDIR"

	; Run at logon for all users
	WriteRegStr HKLM "${AUTORUN_KEY}" "GarageDoor" "$INSTDIR\GarageDoor.jar"

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
	CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\GarageDoor.lnk" "$INSTDIR\GarageDoor.jar" "" "$INSTDIR\GarageDoor.ico" 0
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
