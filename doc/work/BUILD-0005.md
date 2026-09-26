# BUILD-0005 — Release packaging convergence

## Goal

Converge only the remaining rc2 packaging/toolchain warnings without changing application behavior.

## Inputs

- rc2 clean debug/lint build: successful; 0 errors / 5 warnings.
- rc2 release build: successful.
- verified Gradle 9.7.1 / JBR 25.0.3 workstation.
- approved independent STR Remote icon artwork.

## Changes

- package launcher icon as solid adaptive background + transparent foreground motif + monochrome motif;
- remove app-packaged full-square source artwork and duplicate v33 adaptive XML;
- pin daemon JVM major version 25;
- align CI JDK runtime to 25;
- increment candidate to 0.1.0-rc3 / versionCode 6.

## Explicit non-goals

- no feature changes;
- no STR API/discovery changes;
- no Gradle 9.8 upgrade;
- no new dependency;
- no release signing yet.

## Required evidence

- wrapper version/daemon runtime;
- clean debug + lint;
- release assembly;
- normal/themed icon screenshots;
- remaining interaction/reconnect/permission/control checks.
