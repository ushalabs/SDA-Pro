package edu.sdapro.enrichment;

// PATTERN: Abstract Factory
// RATIONALE: Enrichment providers are created as compatible families for demo and enterprise profiles.
public interface EnrichmentProviderFactory {
  String createGeoProvider();
  String createThreatIntelProvider();
  String createAssetProvider();
}

class StandardEnrichmentFactory implements EnrichmentProviderFactory {
  public String createGeoProvider() { return "MockGeoLite"; }
  public String createThreatIntelProvider() { return "VirusTotalMock"; }
  public String createAssetProvider() { return "MockAssetDB"; }
}
