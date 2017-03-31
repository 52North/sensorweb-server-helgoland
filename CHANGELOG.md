# 2.x

## 2.0.0

### CHECK
- SOS `om:parameter`s are added to `feature`/`platform`

### Changes
- extend interface to allow multiple types of sensor platforms and observations
- `offering` and `procedure` are now hierarchical
- `AbstractValue` outputs can have additional parameter information
- separated impl projects (fotoquest and dwd) to own repositories
- pluralized filter parameters
- output includes href property

### Features
- #232 possibility to use `domainId` instead of database id
- #266 `AbstractValue` can have time intervals
- #312 support for using `now`, e.g. `timespan=PT4H/now`
- #304 flexible prerendering title config via placeholders

### Issues 
- #286 prerendering config does not allow overriding general settings

## 1.x

TBD
