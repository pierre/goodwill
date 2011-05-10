# Copyright 2011 Ning, Inc.
#
# Ning licenses this file to you under the Apache License, version 2.0
# (the "License"); you may not use this file except in compliance with the
# License.  You may obtain a copy of the License at:
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations
# under the License.
#

require 'rubygems'
require 'json'

class Hash
    def method_missing(method_name_raw, *args, &block)
        method_name = method_name_raw.to_s
        if method_name =~ /=/
            store(method_name.gsub(/\s*=\s*/, ''), args[0])
        else
            fetch(method_name, nil)
        end
    end
end

# Stupid deprecation warnings
def silently(&block)
  warn_level = $VERBOSE
  $VERBOSE = nil
  begin
    result = block.call
  ensure
    $VERBOSE = warn_level
  end
  result
end

THRIFT_GOODWILL_MAPPINGS = {
    "i64" => "long", # Warning! This could be a DATE!
    "string" => "string"
}

thrift_file = File.open(ARGV[0], 'r')

schema = Hash.new
schema.name = ARGV[0].gsub(/.thrift/, '').split("/")[-1]
schema.schema = []
thrift_file.readlines.each do |entry|
    if entry =~ /^\s*\d*:/
        # entry is something like 1:i64 event_date
        matcher = entry.match /(\d*):(.[^\s]*)\s*(.[^,\n]*),?/
        position = matcher[1].to_i
        type = matcher[2]
        name = matcher[3]
        # {
        #   "name": "is_like",
        #   "type": "BOOLEAN",
        #   "position": 15,
        #   "description": "True for likes, false for unlikes (deletion of likes).",
        #   "sql": {
        #     "type": "boolean",
        #     "length": "",
        #     "scale": "",
        #     "precision": ""
        #   }
        # }
        goodwill_entry = {}
        goodwill_entry.name = name
        silently do
            goodwill_entry.type = THRIFT_GOODWILL_MAPPINGS[type]
        end
        goodwill_entry.position = position
        goodwill_entry.description = "TODO!"
        goodwill_entry.sql = {}
        # TODO....
        silently do
            if type == "string"
                goodwill_entry.sql.type = "varchar"
                goodwill_entry.sql.length = 100
            elsif type == "i64"
                goodwill_entry.sql.type = "bigint"
            end
        end
        schema.schema << goodwill_entry
    end
end

puts schema.to_json
