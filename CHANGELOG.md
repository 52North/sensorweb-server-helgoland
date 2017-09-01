# 2.x

## 2.0.0

### Changes
- extend interface to allow multiple types of sensor platforms and observations
- `offering` and `procedure` are now hierarchical
- `AbstractValue` outputs can have additional parameter information
- `FeatureOutput` and `PlatformOutput` can have additional parameter information
- separated impl projects (fotoquest and dwd) to own repositories
- pluralized filter parameters
- output includes href property
- filterable output members via `fields=<members>` parameter

### Features
- #129 provides paging headers if backend supports counting
- #232 possibility to use `domainId` instead of database id
- #266 `AbstractValue` can have time intervals
- #304 flexible prerendering title config via placeholders
- #312 support for using `now`, e.g. `timespan=PT4H/now`
- #370 support for profile observation
- #403 Support for result time aggregated data output

### Issues 
- #286 prerendering config does not allow overriding general settings
- #334 csv-export for endpoint datasets
- #366 single filters do not work anymore
- #371 csv export errors
- #406 response codes are wrong

## 1.x

TBD
