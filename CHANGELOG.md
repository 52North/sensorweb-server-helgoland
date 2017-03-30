# 2.x

## 2.0.0

### Changes
- extend interface to allow multiple types of sensor platforms and observations
- `offering` and `procedure` are now hierarchical
- result time extension
- SOS `om:parameter`s are added to `feature`/`platform`
- SOS observation parameters are mapped to data output
- separated impl projects (fotoquest and dwd) to own repositories
- `service` is now a full qualified entity
- pluralized filter parameters
- output includes href property

### Features
- #232 possibility to use `domainId` instead of database id
- #251 allow offering to be full qualified parameter
- #266 observations having time intervals
- #312 support for using `now`, e.g. `timespan=PT4H/now`
- #304 flexible prerendering title config via placeholders
- #320 register configurable timezone type

### Issues 
- #246 no use of deleted flag in `series` table
- #279 Querying multiple (time-)series causes race condition 
- #286 prerendering config does not allow overriding general settings

## 1.x

TBD
